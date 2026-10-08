// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/** 手动裁剪头像：拖拽移动、双指缩放，取框内的方形区域 */
public class CropActivity extends Activity {

    public static final String EXTRA_SRC = "src";
    public static final String EXTRA_DST = "dst";

    private CropView cropView;
    private String dst;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Store store = new Store(this);
        UiKit.boot(this, store);

        String src = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_SRC);
        dst = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_DST);
        if (src == null || !new File(src).exists() || dst == null) {
            Toast.makeText(this, "图片有问题，重选一张", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Bitmap bmp = null;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(src, o);
            int sample = 1;
            while (o.outWidth / sample > 2048 || o.outHeight / sample > 2048) {
                sample *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            bmp = BitmapFactory.decodeFile(src, o2);
        } catch (Exception ignored) {
        }
        if (bmp == null) {
            Toast.makeText(this, "这张图读不出来", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LinearLayout root = UiKit.column(this);
        root.setBackgroundColor(0xFF101114);
        root.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 20), UiKit.dp(this, 16), UiKit.dp(this, 16));

        root.addView(UiKit.label(this, "拖动图片，取方框里的部分做头像", 13, 0xFF9AA1AA, false,
                        Gravity.CENTER),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 14));

        cropView = new CropView(this, bmp);
        int side = (int) (getResources().getDisplayMetrics().widthPixels * 0.86);
        FrameLayout holder = new FrameLayout(this);
        holder.addView(cropView, new FrameLayout.LayoutParams(side, side));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(side, side);
        hp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(holder, hp);

        LinearLayout btnRow = UiKit.row(this);
        btnRow.setGravity(Gravity.CENTER);
        TextView cancel = UiKit.button(this, "取消", 0xFFE8EAED, 0x33FFFFFF, 22,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
        TextView ok = UiKit.button(this, "就用这块", 0xFFFFFFFF, UiKit.ACCENT, 22,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        save();
                    }
                });
        btnRow.addView(cancel);
        btnRow.addView(ok, UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 12, 0, 0, 0));
        root.addView(btnRow, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 22, 0, 0));

        setContentView(root);
    }

    private void save() {
        try {
            Bitmap out = cropView.crop();
            if (out == null) {
                Toast.makeText(this, "裁不出来，换个位置试试", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap small = Bitmap.createScaledBitmap(out, 512, 512, true);
            File f = new File(dst);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(f);
            small.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_DST, f.getAbsolutePath()));
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败：" + e.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }

    /** 可拖动缩放的取景框 */
    private static class CropView extends View {

        private final Bitmap bmp;
        private final Matrix matrix = new Matrix();
        private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        private final Paint frame = new Paint();
        private float baseScale = 1f;
        private float scale = 1f;
        private float dx = 0f;
        private float dy = 0f;
        private float lastX;
        private float lastY;
        private ScaleGestureDetector scaleDetector;

        CropView(Context c, Bitmap bmp) {
            super(c);
            this.bmp = bmp;
            frame.setStyle(Paint.Style.STROKE);
            frame.setStrokeWidth(UiKit.dp(c, 2));
            frame.setColor(0x99FFFFFF);
            scaleDetector = new ScaleGestureDetector(c,
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        public boolean onScale(ScaleGestureDetector d) {
                            float target = scale * d.getScaleFactor();
                            float min = baseScale;
                            float max = baseScale * 4f;
                            if (target < min) {
                                target = min;
                            }
                            if (target > max) {
                                target = max;
                            }
                            float k = target / scale;
                            scale = target;
                            dx = d.getFocusX() - (d.getFocusX() - dx) * k;
                            dy = d.getFocusY() - (d.getFocusY() - dy) * k;
                            clamp();
                            apply();
                            invalidate();
                            return true;
                        }
                    });
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            baseScale = Math.max((float) w / bmp.getWidth(), (float) h / bmp.getHeight());
            scale = baseScale;
            dx = (w - bmp.getWidth() * scale) / 2f;
            dy = (h - bmp.getHeight() * scale) / 2f;
            clamp();
            apply();
        }

        private void apply() {
            matrix.reset();
            matrix.postScale(scale, scale);
            matrix.postTranslate(dx, dy);
        }

        private void clamp() {
            float w = bmp.getWidth() * scale;
            float h = bmp.getHeight() * scale;
            if (dx > 0) {
                dx = 0;
            }
            if (dy > 0) {
                dy = 0;
            }
            if (dx + w < getWidth()) {
                dx = getWidth() - w;
            }
            if (dy + h < getHeight()) {
                dy = getHeight() - h;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            scaleDetector.onTouchEvent(e);
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = e.getX();
                    lastY = e.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!scaleDetector.isInProgress() && e.getPointerCount() == 1) {
                        dx += e.getX() - lastX;
                        dy += e.getY() - lastY;
                        lastX = e.getX();
                        lastY = e.getY();
                        clamp();
                        apply();
                        invalidate();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(bmp, matrix, paint);
            canvas.drawRect(1, 1, getWidth() - 1, getHeight() - 1, frame);
        }

        Bitmap crop() {
            try {
                Matrix inv = new Matrix();
                if (!matrix.invert(inv)) {
                    return null;
                }
                float[] pts = new float[]{0, 0, getWidth(), getHeight()};
                inv.mapPoints(pts);
                int x = Math.max(0, (int) pts[0]);
                int y = Math.max(0, (int) pts[1]);
                int w = Math.min(bmp.getWidth() - x, (int) (pts[2] - pts[0]));
                int h = Math.min(bmp.getHeight() - y, (int) (pts[3] - pts[1]));
                int side = Math.min(w, h);
                if (side <= 0) {
                    return null;
                }
                return Bitmap.createBitmap(bmp, x, y, side, side);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
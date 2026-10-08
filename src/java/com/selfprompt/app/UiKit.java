// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 界面基础件与配色。消息类型：0 = AI，1 = 用户，2 = 系统提示条 */
public class UiKit {

    public static int BG = 0xFFF5F6F8;
    public static int SURFACE = 0xFFFFFFFF;
    public static int TOPBAR = 0xFFFFFFFF;
    public static int ACCENT = 0xFF4F6BFF;
    public static int TEXT = 0xFF14161A;
    public static int TEXT_SUB = 0xFF8A9099;
    public static int BORDER = 0xFFE8EAEE;
    public static int CHIP_BG = 0xFFF0F1F4;
    public static int NOTE_BG = 0xFFFFF7E6;
    public static int NOTE_TEXT = 0xFF8A6A1F;
    public static int NOTE_BORDER = 0xFFF2E2BE;
    public static int THINK_BG = 0xFFF1F2F5;
    public static int DISABLED = 0xFFC3C8D0;
    public static int DANGER = 0xFFE05252;
    public static int AVATAR_AI = ACCENT;
    public static int AVATAR_USER = 0xFFDDE2EA;
    /** 头像上的字色 */
    public static int AVATAR_USER_TEXT = 0xFF5A6270;
    /** 淡色按钮底（小按钮用） */
    public static int ACCENT_SOFT = 0xFFEAF0FE;
    /** 卡片里再嵌一层的小块底色，例如版本历史的一条 */
    public static int INNER_BG = 0xFFF8F9FB;
    /** 输入框底色：比卡片底略深一点，边界看得清 */
    public static int INPUT_BG = 0xFFF0F1F4;

    public static boolean isDarkTheme = false;

    /** 切主题：所有颜色在这里换一套 */
    public static void applyTheme(boolean dark) {
        isDarkTheme = dark;
        if (dark) {
            BG = 0xFF111214;
            SURFACE = 0xFF1B1D21;
            TOPBAR = 0xFF1B1D21;
            ACCENT = 0xFF6B84FF;
            TEXT = 0xFFE8EAED;
            TEXT_SUB = 0xFF8B9199;
            BORDER = 0xFF2A2D33;
            CHIP_BG = 0xFF26292F;
            NOTE_BG = 0xFF3A3324;
            NOTE_TEXT = 0xFFE3C88E;
            NOTE_BORDER = 0xFF4B4230;
            THINK_BG = 0xFF22252A;
            DISABLED = 0xFF3A3E45;
            DANGER = 0xFFE86B6B;
            AVATAR_AI = ACCENT;
            AVATAR_USER = 0xFF2E3238;
            AVATAR_USER_TEXT = 0xFFC9CED6;
            ACCENT_SOFT = 0xFF2A3355;
            INNER_BG = 0xFF23262B;
            INPUT_BG = 0xFF26292F;
        } else {
            BG = 0xFFF5F6F8;
            SURFACE = 0xFFFFFFFF;
            TOPBAR = 0xFFFFFFFF;
            ACCENT = 0xFF4F6BFF;
            TEXT = 0xFF14161A;
            TEXT_SUB = 0xFF8A9099;
            BORDER = 0xFFE8EAEE;
            CHIP_BG = 0xFFF0F1F4;
            NOTE_BG = 0xFFFFF7E6;
            NOTE_TEXT = 0xFF8A6A1F;
            NOTE_BORDER = 0xFFF2E2BE;
            THINK_BG = 0xFFF1F2F5;
            DISABLED = 0xFFC3C8D0;
            DANGER = 0xFFE05252;
            AVATAR_AI = ACCENT;
            AVATAR_USER = 0xFFDDE2EA;
            AVATAR_USER_TEXT = 0xFF5A6270;
            ACCENT_SOFT = 0xFFEAF0FE;
            INNER_BG = 0xFFF8F9FB;
            INPUT_BG = 0xFFF0F1F4;
        }
    }

    // ================= 一套外观一次装好 =================

    /** 每个界面的 onCreate 都走这里：主题、按钮颜色、磨砂、背景图、字体、动画开关 */
    public static void boot(Context c, Store s) {
        s.applyLang();
        animationsOn = s.animOn();
        applyTheme(s.isDark());
        setAccent(s.accentColor(isDarkTheme));
        setFrost(s.frosted() ? s.frostPercent() : 0);
        loadBackground(c, s.bgPath(), s.bgClarity(), s.bgBlur());
        loadFont(s.fontPath());
    }

    /** 外观的指纹：几项设置里任何一项变了，值就不一样，用来判断要不要重画 */
    public static String styleKey(Store s) {
        return (s.isDark() ? "d" : "l") + "|" + s.accentColor(s.isDark()) + "|" + s.frosted()
                + "|" + s.frostPercent() + "|" + s.bgPath() + "|" + s.bgClarity() + "|"
                + s.bgBlur() + "|" + s.fontPath() + "|" + s.chatMode() + "|" + s.resolvedLang();
    }

    /** color 传 0 表示用主题自带的那支颜色 */
    public static void setAccent(int color) {
        if (color == 0) {
            return;
        }
        ACCENT = color;
        AVATAR_AI = color;
        ACCENT_SOFT = (0x1F << 24) | (color & 0x00FFFFFF);
    }

    /** 磨砂玻璃：把面板颜色变半透明，底下的背景图才透得出来 */
    public static void setFrost(int percent) {
        int p = percent;
        if (p < 0) {
            p = 0;
        }
        if (p > 85) {
            p = 85;
        }
        if (p == 0) {
            return;
        }
        int alpha = 255 - (255 * p / 100);
        BG = withAlpha(BG, alpha);
        SURFACE = withAlpha(SURFACE, alpha);
        TOPBAR = withAlpha(TOPBAR, alpha);
        CHIP_BG = withAlpha(CHIP_BG, alpha);
        THINK_BG = withAlpha(THINK_BG, alpha);
        INNER_BG = withAlpha(INNER_BG, alpha);
    }

    public static int withAlpha(int color, int alpha) {
        int a = alpha;
        if (a < 0) {
            a = 0;
        }
        if (a > 255) {
            a = 255;
        }
        return (a << 24) | (color & 0x00FFFFFF);
    }

    // ================= 背景图 =================

    private static Bitmap bgBitmap = null;
    private static String bgKey = "";

    /** 按清晰度和模糊度数把图准备好；路径为空就清掉背景 */
    public static void loadBackground(Context c, String path, int clarity, int blur) {
        String key = (path == null ? "" : path) + "|" + clarity + "|" + blur;
        if (key.equals(bgKey)) {
            return;
        }
        bgKey = key;
        bgBitmap = null;
        if (path == null || path.trim().isEmpty()) {
            return;
        }
        try {
            java.io.File f = new java.io.File(path.trim());
            if (!f.exists()) {
                return;
            }
            int maxSide = Math.max(200, clarity * 10);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            int sample = 1;
            while (o.outWidth / (sample * 2) >= maxSide || o.outHeight / (sample * 2) >= maxSide) {
                sample *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
            if (bmp == null) {
                return;
            }
            bmp = scaleTo(bmp, maxSide);
            bmp = cropToScreen(c, bmp);
            if (blur > 0) {
                int radius = Math.max(1, blur * bmp.getWidth() / 500);
                bmp = blurBitmap(bmp, radius);
            }
            bgBitmap = bmp;
        } catch (Exception e) {
            bgBitmap = null;
        } catch (OutOfMemoryError e) {
            bgBitmap = null;
        }
    }

    private static Bitmap scaleTo(Bitmap src, int maxSide) {
        int w = src.getWidth();
        int h = src.getHeight();
        int longer = Math.max(w, h);
        if (longer <= maxSide) {
            return src;
        }
        float k = (float) maxSide / (float) longer;
        int nw = Math.max(1, (int) (w * k));
        int nh = Math.max(1, (int) (h * k));
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }

    /** 裁成屏幕比例，铺满的时候才不会把人脸拉长 */
    private static Bitmap cropToScreen(Context c, Bitmap src) {
        try {
            android.util.DisplayMetrics dm = c.getResources().getDisplayMetrics();
            if (dm.widthPixels <= 0 || dm.heightPixels <= 0) {
                return src;
            }
            float target = (float) dm.widthPixels / (float) dm.heightPixels;
            int w = src.getWidth();
            int h = src.getHeight();
            float cur = (float) w / (float) h;
            if (cur > target) {
                int nw = Math.max(1, (int) (h * target));
                return Bitmap.createBitmap(src, Math.max(0, (w - nw) / 2), 0, nw, h);
            }
            if (cur < target) {
                int nh = Math.max(1, (int) (w / target));
                return Bitmap.createBitmap(src, 0, Math.max(0, (h - nh) / 2), w, nh);
            }
        } catch (Exception ignored) {
        }
        return src;
    }

    /** 两轮方框模糊，够像高斯了，而且跟前缀和一样是线性的，再大的半径也不卡 */
    private static Bitmap blurBitmap(Bitmap src, int radius) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] pix = new int[w * h];
        src.getPixels(pix, 0, w, 0, 0, w, h);
        for (int pass = 0; pass < 2; pass++) {
            pix = blurAxis(pix, w, h, radius, true);
            pix = blurAxis(pix, w, h, radius, false);
        }
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pix, 0, w, 0, 0, w, h);
        return out;
    }

    private static int[] blurAxis(int[] pix, int w, int h, int r, boolean horizontal) {
        int[] out = new int[pix.length];
        int n = horizontal ? w : h;
        int lines = horizontal ? h : w;
        long[] sa = new long[n + 1];
        long[] sr = new long[n + 1];
        long[] sg = new long[n + 1];
        long[] sb = new long[n + 1];
        for (int i = 0; i < lines; i++) {
            sa[0] = 0;
            sr[0] = 0;
            sg[0] = 0;
            sb[0] = 0;
            for (int j = 0; j < n; j++) {
                int p = pix[horizontal ? i * w + j : j * w + i];
                sa[j + 1] = sa[j] + (p >>> 24);
                sr[j + 1] = sr[j] + ((p >> 16) & 0xFF);
                sg[j + 1] = sg[j] + ((p >> 8) & 0xFF);
                sb[j + 1] = sb[j] + (p & 0xFF);
            }
            for (int j = 0; j < n; j++) {
                int lo = j - r < 0 ? 0 : j - r;
                int hi = j + r > n - 1 ? n - 1 : j + r;
                int cnt = hi - lo + 1;
                int a = (int) ((sa[hi + 1] - sa[lo]) / cnt);
                int rr = (int) ((sr[hi + 1] - sr[lo]) / cnt);
                int gg = (int) ((sg[hi + 1] - sg[lo]) / cnt);
                int bb = (int) ((sb[hi + 1] - sb[lo]) / cnt);
                out[horizontal ? i * w + j : j * w + i] =
                        (a << 24) | (rr << 16) | (gg << 8) | bb;
            }
        }
        return out;
    }

    /** 给界面铺底：有背景图就铺图，没有就用背景色 */
    public static void paintBackground(View v) {
        if (v == null) {
            return;
        }
        if (bgBitmap != null) {
            BitmapDrawable d = new BitmapDrawable(v.getResources(), bgBitmap);
            d.setGravity(Gravity.FILL);
            v.setBackground(d);
        } else {
            v.setBackgroundColor(BG);
        }
    }

    public static boolean hasBackgroundImage() {
        return bgBitmap != null;
    }

    /** 中间的滚动层留透明，让最外层那张图透上来，滚动的时候背景才是稳的 */
    public static void paintTransparent(View v) {
        if (v != null) {
            v.setBackgroundColor(0x00000000);
        }
    }

    // ================= 动效 =================

    /** 界面动画总开关，跟着设置走 */
    public static boolean animationsOn = true;

    /** 一条消息/一张卡片进来：淡入 + 轻微上移 + 一点放大，都是硬件层属性，不改布局 */
    public static void popIn(final View v, long delayMs, float dyDp, float scale) {
        if (!animationsOn || v == null) {
            return;
        }
        v.setAlpha(0f);
        v.setTranslationY(dp(v.getContext(), dyDp));
        if (scale > 0f && scale < 1f) {
            v.setScaleX(scale);
            v.setScaleY(scale);
        }
        v.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delayMs)
                .setDuration(230)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    /** 新气泡进来 */
    public static void bubbleIn(View v) {
        popIn(v, 0, 10f, 0.985f);
    }

    /** 整页进来：根容器淡入，卡片依次错开一点点 */
    public static void pageIn(final View root) {
        if (!animationsOn || root == null) {
            return;
        }
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(200).start();
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            int n = Math.min(g.getChildCount(), 8);
            for (int i = 1; i < n; i++) {
                popIn(g.getChildAt(i), (long) (i - 1) * 55, 14f, 1f);
            }
        }
    }

    /** 一串条目依次错开进入，侧边栏这种列表用 */
    public static void staggerIn(ViewGroup parent, int from, int count, long stepMs, float dyDp) {
        if (!animationsOn || parent == null) {
            return;
        }
        int n = Math.min(parent.getChildCount(), from + count);
        for (int i = from; i < n; i++) {
            popIn(parent.getChildAt(i), (long) (i - from) * stepMs, dyDp, 1f);
        }
    }

    /** 按下缩一点、松开弹回来。返回 false，点击和滚动都不受影响 */
    public static void pressable(final View v) {
        if (v == null) {
            return;
        }
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, android.view.MotionEvent e) {
                if (!animationsOn) {
                    return false;
                }
                int a = e.getActionMasked();
                if (a == android.view.MotionEvent.ACTION_DOWN) {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                } else if (a == android.view.MotionEvent.ACTION_UP
                        || a == android.view.MotionEvent.ACTION_CANCEL) {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(150)
                            .setInterpolator(new android.view.animation.OvershootInterpolator(1.8f))
                            .start();
                }
                return false;
            }
        });
    }

    // ================= 图片缓存 =================

    private static final java.util.HashMap<String, Bitmap> imgCache =
            new java.util.HashMap<String, Bitmap>();

    /** 同一张图不要反复解码，头像和附件缩略图都走这里 */
    public static Bitmap cachedBitmap(String path, int maxPx) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String key = path.trim() + "@" + maxPx;
        Bitmap b = imgCache.get(key);
        if (b != null && !b.isRecycled()) {
            return b;
        }
        try {
            java.io.File f = new java.io.File(path.trim());
            if (!f.exists()) {
                return null;
            }
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path.trim(), o);
            int sample = 1;
            while (o.outWidth / (sample * 2) >= maxPx && o.outHeight / (sample * 2) >= maxPx) {
                sample *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            b = BitmapFactory.decodeFile(path.trim(), o2);
            if (b != null) {
                if (imgCache.size() > 24) {
                    imgCache.clear();
                }
                imgCache.put(key, b);
            }
            return b;
        } catch (Exception e) {
            return null;
        } catch (OutOfMemoryError e) {
            imgCache.clear();
            return null;
        }
    }

    public static Bitmap cachedBitmap(String path) {
        return cachedBitmap(path, 512);
    }

    public static void clearImageCache() {
        imgCache.clear();
    }

    public static final int TYPE_AI = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_NOTE = 2;
    /** 思考段：灰字小一号 */
    public static final int TYPE_THINK = 3;

    private static android.graphics.Typeface customFont = null;
    private static String loadedFontPath = null;

    /** 换字体；path 为空就回到系统字体 */
    public static void loadFont(String path) {
        String p = path == null ? "" : path.trim();
        if (p.equals(loadedFontPath)) {
            return;
        }
        loadedFontPath = p;
        customFont = null;
        if (p.isEmpty()) {
            return;
        }
        try {
            java.io.File f = new java.io.File(p);
            if (f.exists()) {
                customFont = android.graphics.Typeface.createFromFile(f);
            }
        } catch (Exception e) {
            customFont = null;
        }
    }

    public static boolean hasCustomFont() {
        return customFont != null;
    }

    /**
     * 弹窗工厂：应用主题固定是浅色的，弹窗得按当前是不是深色自己挑一套，
     * 否则深色模式下弹窗是白底，里面的字跟着主题是浅色，什么都看不见
     */
    public static AlertDialog.Builder dialog(Context c) {
        int st = isDarkTheme ? R.style.SelfPromptDialogDark : R.style.SelfPromptDialogLight;
        // 必须用两参构造把主题 id 显式传进去：单参构造会拿 ?android:dialogTheme 再包一层，
        // 把我们在样式里挂的 windowBackground（圆角）丢掉，弹窗就还是方块
        return new AlertDialog.Builder(c, st);
    }

    public static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics());
    }

    public static GradientDrawable shape(Context c, int fill, int stroke, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        if (stroke != 0) {
            g.setStroke(Math.max(1, dp(c, 1)), stroke);
        }
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    private static Drawable ripple(Drawable content) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new RippleDrawable(ColorStateList.valueOf(0x1F000000), content, null);
        }
        return content;
    }

    public static TextView label(Context c, String s, float sp, int color, boolean bold, int gravity) {
        TextView tv = new TextView(c);
        tv.setText(s == null ? "" : Lang.t(s));
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setLineSpacing(dp(c, 5), 1f);
        tv.setGravity(gravity);
        if (customFont != null) {
            tv.setTypeface(customFont, bold ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        } else if (bold) {
            tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return tv;
    }

    public static TextView button(Context c, String s, int fg, int bg, float radiusDp,
                                  View.OnClickListener l) {
        TextView tv = label(c, s, 14, fg, false, Gravity.CENTER);
        tv.setPadding(dp(c, 16), dp(c, 9), dp(c, 16), dp(c, 9));
        tv.setBackground(ripple(shape(c, bg, 0, radiusDp)));
        tv.setClickable(true);
        if (l != null) {
            tv.setOnClickListener(l);
        }
        pressable(tv);
        return tv;
    }

    public static TextView smallButton(Context c, String s, int fg, int bg, float radiusDp,
                                       View.OnClickListener l) {
        TextView tv = label(c, s, 12.5f, fg, false, Gravity.CENTER);
        tv.setPadding(dp(c, 12), dp(c, 6), dp(c, 12), dp(c, 6));
        tv.setBackground(ripple(shape(c, bg, 0, radiusDp)));
        tv.setClickable(true);
        if (l != null) {
            tv.setOnClickListener(l);
        }
        pressable(tv);
        return tv;
    }

    public static TextView outlineButton(Context c, String s, int fg, int stroke, float radiusDp,
                                         View.OnClickListener l) {
        TextView tv = label(c, s, 13, fg, false, Gravity.CENTER);
        tv.setPadding(dp(c, 14), dp(c, 8), dp(c, 14), dp(c, 8));
        tv.setBackground(ripple(shape(c, SURFACE, stroke, radiusDp)));
        tv.setClickable(true);
        if (l != null) {
            tv.setOnClickListener(l);
        }
        pressable(tv);
        return tv;
    }
public interface SliderCallback {
        /** done 为 true 表示手松开了，可以拿去做重活儿（例如重建背景图） */
        void onValue(int value, boolean done);
    }

    /** 一个小圆点，用来显示某个颜色 */
    public static View dot(Context c, int color, int sizeDp) {
        View v = new View(c);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        v.setBackground(g);
        int s = dp(c, sizeDp);
        v.setLayoutParams(new LinearLayout.LayoutParams(s, s));
        return v;
    }


    /** 一行滑杆：左边标题、右边当前值、下面 SeekBar */
    public static LinearLayout sliderRow(Context c, String title, int min, int max, int value,
                                         final SliderCallback cb) {
        LinearLayout col = column(c);
        LinearLayout head = row(c);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(label(c, title, 12.5f, TEXT_SUB, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final TextView val = label(c, String.valueOf(value), 12.5f, ACCENT, true, Gravity.END);
        head.addView(val);
        col.addView(head);

        android.widget.SeekBar sb = new android.widget.SeekBar(c);
        sb.setMax(Math.max(1, max - min));
        sb.setProgress(Math.max(0, value - min));
        try {
            sb.setProgressTintList(ColorStateList.valueOf(ACCENT));
            sb.setThumbTintList(ColorStateList.valueOf(ACCENT));
        } catch (Exception ignored) {
        }
        sb.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(android.widget.SeekBar s, int p, boolean fromUser) {
                val.setText(String.valueOf(p + min));
                if (cb != null) {
                    cb.onValue(p + min, false);
                }
            }

            public void onStartTrackingTouch(android.widget.SeekBar s) {
            }

            public void onStopTrackingTouch(android.widget.SeekBar s) {
                if (cb != null) {
                    cb.onValue(s.getProgress() + min, true);
                }
            }
        });
        col.addView(sb);
        return col;
    }

    public static EditText field(Context c, String hint, String value) {
        EditText e = new EditText(c);
        e.setHint(Lang.t(hint));
        e.setText(value == null ? "" : value);
        e.setTextSize(15);
        e.setTextColor(TEXT);
        e.setHintTextColor(0xFFA6ADB4);
        e.setBackground(shape(c, INPUT_BG, BORDER, 14));
        e.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        return e;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout.LayoutParams lp(int w, int h, float weight, Context c,
                                               int ml, int mt, int mr, int mb) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h, weight);
        p.setMargins(dp(c, ml), dp(c, mt), dp(c, mr), dp(c, mb));
        return p;
    }

    /** 一条消息整行（带对齐） */
    public static LinearLayout messageRow(Context c, int type) {
        LinearLayout row = row(c);
        if (type == TYPE_USER) {
            row.setGravity(Gravity.END);
        } else if (type == TYPE_NOTE) {
            row.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            row.setGravity(Gravity.START);
        }
        return row;
    }

    /** 圆形头像：给消息加个身份标识 */
    public static TextView avatar(Context c, String name, boolean isUser) {
        String s = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1);
        TextView tv = label(c, s, 12.5f, isUser ? AVATAR_USER_TEXT : 0xFFFFFFFF, true, Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(isUser ? AVATAR_USER : AVATAR_AI);
        tv.setBackground(g);
        int size = dp(c, 30);
        tv.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return tv;
    }

    /** 头像位：有自定义图片就用图片，否则用首字 */
    public static View avatarView(Context c, String name, String imagePath, boolean isUser, int sizeDp) {
        int size = dp(c, sizeDp);
        if (imagePath != null && !imagePath.trim().isEmpty()
                && new java.io.File(imagePath).exists()) {
            android.widget.ImageView iv = new android.widget.ImageView(c);
            try {
                android.graphics.Bitmap bm = cachedBitmap(imagePath, 256);
                if (bm != null) {
                    iv.setImageBitmap(bm);
                    iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    iv.setClipToOutline(true);
                    GradientDrawable g = new GradientDrawable();
                    g.setShape(GradientDrawable.OVAL);
                    g.setColor(isUser ? AVATAR_USER : AVATAR_AI);
                    iv.setBackground(g);
                    iv.setLayoutParams(new LinearLayout.LayoutParams(size, size));
                    return iv;
                }
            } catch (Exception ignored) {
            }
        }
        TextView tv = label(c, (name == null || name.isEmpty()) ? "?" : name.substring(0, 1),
                sizeDp >= 44 ? 20 : 12.5f, isUser ? AVATAR_USER_TEXT : 0xFFFFFFFF, true, Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(isUser ? AVATAR_USER : AVATAR_AI);
        tv.setBackground(g);
        tv.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return tv;
    }

    /** 头像位占位，保证多段消息左边对齐 */
    public static View avatarSpacer(Context c) {
        View v = new View(c);
        int size = dp(c, 30);
        v.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return v;
    }

    /** 日期分隔 */
    public static TextView dayDivider(Context c, String text) {
        TextView tv = label(c, text, 11.5f, TEXT_SUB, false, Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setColor(0x22000000);
        g.setCornerRadius(dp(c, 9));
        tv.setBackground(g);
        tv.setPadding(dp(c, 10), dp(c, 4), dp(c, 10), dp(c, 4));
        return tv;
    }
/** 只做一个气泡本体，方便外面自己拼头像 */
    public static TextView bubbleView(Context c, String text, int type) {
        float sp = (type == TYPE_NOTE) ? 12.5f : (type == TYPE_THINK ? 13f : 15f);
        int color;
        if (type == TYPE_USER) {
            color = 0xFFFFFFFF;
        } else if (type == TYPE_NOTE) {
            color = NOTE_TEXT;
        } else if (type == TYPE_THINK) {
            color = 0xFF7C838F;
        } else {
            color = TEXT;
        }
        TextView tv = label(c, "", sp, color, false, Gravity.START);
        if (type == TYPE_AI || type == TYPE_THINK) {
            tv.setText(Markdown.render(text));
        } else {
            tv.setText(text == null ? "" : text);
        }
        tv.setTextIsSelectable(true);
        tv.setMaxWidth((int) (c.getResources().getDisplayMetrics().widthPixels * 0.78f));

        int fill;
        int stroke;
        if (type == TYPE_USER) {
            fill = ACCENT;
            stroke = 0;
        } else if (type == TYPE_NOTE) {
            fill = NOTE_BG;
            stroke = NOTE_BORDER;
        } else if (type == TYPE_THINK) {
            fill = THINK_BG;
            stroke = 0;
        } else {
            fill = SURFACE;
            stroke = BORDER;
        }
        tv.setBackground(shape(c, fill, stroke, (type == TYPE_NOTE) ? 14 : 24));
        tv.setPadding(dp(c, 16), dp(c, 11), dp(c, 16), dp(c, 11));
        return tv;
    }

    /** 气泡容器，用于装图片或自定义内容 */
    public static LinearLayout bubbleBox(Context c, boolean isUser) {
        LinearLayout l = column(c);
        l.setBackground(shape(c, isUser ? ACCENT : SURFACE, isUser ? 0 : BORDER, 24));
        l.setPadding(dp(c, 16), dp(c, 11), dp(c, 16), dp(c, 11));
        return l;
    }

    /** 一条消息整行（带对齐） */
    public static LinearLayout message(Context c, String text, int type) {
        LinearLayout row = row(c);
        if (type == TYPE_USER) {
            row.setGravity(Gravity.END);
        } else if (type == TYPE_NOTE) {
            row.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            row.setGravity(Gravity.START);
        }

        float sp = (type == TYPE_NOTE) ? 12.5f : (type == TYPE_THINK ? 13f : 15f);
        int color;
        if (type == TYPE_USER) {
            color = 0xFFFFFFFF;
        } else if (type == TYPE_NOTE) {
            color = NOTE_TEXT;
        } else if (type == TYPE_THINK) {
            color = 0xFF7C838F;
        } else {
            color = TEXT;
        }
        TextView tv = bubbleView(c, text, type);
        row.addView(tv);
        return row;
    }

    /** 取消息行里的气泡本体，用于流式更新 */
    public static TextView bubbleText(View messageRow) {
        if (messageRow instanceof LinearLayout) {
            LinearLayout l = (LinearLayout) messageRow;
            if (l.getChildCount() > 0 && l.getChildAt(0) instanceof TextView) {
                return (TextView) l.getChildAt(0);
            }
        }
        return null;
    }

    public static void addMessage(LinearLayout box, Context c, String text, int type) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(c, (type == TYPE_NOTE) ? 8 : 5), 0, dp(c, 5));
        box.addView(message(c, text, type), p);
    }

    /** 白色卡片 */
    public static void card(LinearLayout parent, View v, Context c) {
        v.setBackground(shape(c, SURFACE, BORDER, 18));
        parent.addView(v, lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, c, 0, 10, 0, 0));
    }

    public static TextView sectionTitle(Context c, String s) {
        TextView tv = label(c, s, 12.5f, TEXT_SUB, true, Gravity.START);
        tv.setLetterSpacing(0.08f);
        return tv;
    }

    /** 处理全面屏：顶部让开状态栏，底部让开输入法/导航栏 */
    public static void edgeToEdge(final Activity act, final View topView, final int topBasePad,
                                  final View bottomView, final int bottomBasePad,
                                  final boolean darkStatusIcons) {
        if (Build.VERSION.SDK_INT >= 30) {
            act.getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c = act.getWindow().getInsetsController();
            if (c != null) {
                boolean darkIcons = darkStatusIcons && !isDarkTheme;
                if (darkIcons) {
                    c.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                } else {
                    c.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            View d = act.getWindow().getDecorView();
            int f = d.getSystemUiVisibility();
            boolean darkIcons = darkStatusIcons && !isDarkTheme;
            if (darkIcons) {
                f = f | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                f = f & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            d.setSystemUiVisibility(f);
        }

        View content = act.getWindow().getDecorView().findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int top = 0;
                int bottom = 0;
                if (Build.VERSION.SDK_INT >= 30) {
                    android.graphics.Insets sb = insets.getInsets(WindowInsets.Type.systemBars());
                    android.graphics.Insets im = insets.getInsets(WindowInsets.Type.ime());
                    top = sb.top;
                    bottom = Math.max(sb.bottom, im.bottom);
                } else {
                    top = insets.getSystemWindowInsetTop();
                    bottom = insets.getSystemWindowInsetBottom();
                }
                if (topView != null) {
                    topView.setPadding(topView.getPaddingLeft(), topBasePad + top,
                            topView.getPaddingRight(), topView.getPaddingBottom());
                }
                if (bottomView != null) {
                    bottomView.setPadding(bottomView.getPaddingLeft(), bottomView.getPaddingTop(),
                            bottomView.getPaddingRight(), bottomBasePad + bottom);
                }
                return insets;
            }
        });
        content.requestApplyInsets();
    }
}
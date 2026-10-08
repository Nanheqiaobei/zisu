// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** 外观设置：主题、按钮颜色、背景图、磨砂玻璃、动画、字体 */
public class AppearanceActivity extends Activity {

    private static final int REQ_BG = 3001;
    private static final int REQ_FONT = 3002;

    private Store store;
    private ScrollView sv;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        UiKit.boot(this, store);

        sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        setContentView(sv);

        rebuildNow(true);
    }

    /** 事件里改完设置不要当场重画，等这一轮触摸走完再画，滑杆才不会被拆在半路上 */
    private void rebuildLater(final boolean animate) {
        sv.post(new Runnable() {
            public void run() {
                rebuildNow(animate);
            }
        });
    }

    /**
     * 就地重画这一页。以前是调用 recreate()，整页重建会跳回最上面，改一个滑杆难受一次。
     * 现在只换内容，滚动位置留着。
     */
    private void rebuildNow(boolean animate) {
        UiKit.boot(this, store);
        UiKit.paintBackground(sv);
        final int y = sv.getScrollY();

        sv.removeAllViews();
        LinearLayout root = buildContent();
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);

        sv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        if (sv.getViewTreeObserver().isAlive()) {
                            sv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        sv.scrollTo(0, y);
                    }
                });
        if (animate) {
            UiKit.pageIn(root);
        }
    }

    private LinearLayout buildContent() {
        LinearLayout root = UiKit.column(this);
        UiKit.paintTransparent(root);
        root.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 28));

        LinearLayout top = UiKit.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(UiKit.label(this, "外观设置", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 18));

        buildPreview(root);
        buildThemeCard(root);
        buildColorCard(root);
        buildBackgroundCard(root);
        buildFrostCard(root);
        buildAnimCard(root);
        buildSplashCard(root);
        buildFontCard(root);

        root.addView(UiKit.label(this,
                "这里的每一项都是改完立刻生效，不会再跳回最上面。背景图存在本机，不会上传",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 4, 16, 4, 0));
        return root;
    }

    // ============ 小样 ============

    private void buildPreview(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "小样"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        LinearLayout row = UiKit.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(UiKit.button(this, "主要按钮", 0xFFFFFFFF, UiKit.ACCENT, 20, null));
        row.addView(UiKit.smallButton(this, "次要按钮", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10, null),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 10, 0, 0, 0));
        row.addView(UiKit.dot(this, UiKit.ACCENT, 22), UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 10, 0, 0, 0));
        card.addView(row);

        TextView bubble = UiKit.bubbleView(this, "我这边看到的就是这套颜色", UiKit.TYPE_AI);
        card.addView(bubble, UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 0));
        UiKit.card(root, card, this);
    }

    // ============ 主题 ============

    private void buildThemeCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "主题"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        card.addView(UiKit.label(this, "从设置里搬过来的，浅色、深色、跟随系统",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        LinearLayout row = UiKit.row(this);
        final String[] names = new String[]{"跟随系统", "浅色", "深色"};
        for (int i = 0; i < names.length; i++) {
            final int mode = i;
            boolean on = store.themeMode() == i;
            TextView btn = UiKit.smallButton(this, names[i],
                    on ? 0xFFFFFFFF : UiKit.TEXT_SUB, on ? UiKit.ACCENT : UiKit.CHIP_BG, 10,
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            store.setThemeMode(mode);
                            rebuildLater(true);
                        }
                    });
            row.addView(btn, UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 8, 0));
        }
        card.addView(row);
        UiKit.card(root, card, this);
    }

    // ============ 按钮颜色 ============

    private void buildColorCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "按钮与强调色"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        card.addView(UiKit.label(this, "按钮、气泡、头像、侧边栏图标都会跟着换",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        final int cur = store.accentPreset();
        boolean custom = !store.accentCustom().isEmpty();

        for (int i = 0; i < Store.ACCENT_NAMES.length; i++) {
            final int idx = i;
            boolean on = !custom && cur == i;
            LinearLayout row = UiKit.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(UiKit.shape(this, on ? UiKit.ACCENT_SOFT : 0x00000000,
                    on ? UiKit.ACCENT : UiKit.BORDER, 14));
            row.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 10), UiKit.dp(this, 12), UiKit.dp(this, 10));
            row.addView(UiKit.dot(this, UiKit.isDarkTheme ? Store.ACCENT_DARK[i]
                    : Store.ACCENT_LIGHT[i], 20));
            row.addView(UiKit.label(this, Store.ACCENT_NAMES[i], 14, UiKit.TEXT, on, Gravity.START),
                    UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, this, 12, 0, 0, 0));
            row.addView(UiKit.label(this, on ? "使用中" : "", 11.5f, UiKit.ACCENT, false, Gravity.CENTER));
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    store.setAccentPreset(idx);
                    store.setAccentCustom("");
                    rebuildLater(true);
                }
            });
            UiKit.pressable(row);
            card.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
        }

        String cu = store.accentCustom();
        LinearLayout crow = UiKit.row(this);
        crow.setGravity(Gravity.CENTER_VERTICAL);
        crow.addView(UiKit.dot(this, custom ? UiKit.ACCENT : UiKit.CHIP_BG, 20));
        crow.addView(UiKit.label(this, custom ? ("自定义 " + cu) : "自定义颜色搭配", 14,
                        UiKit.TEXT, custom, Gravity.START),
                UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, this, 12, 0, 0, 0));
        crow.addView(UiKit.smallButton(this, "调配", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        openColorPicker();
                    }
                }));
        card.addView(crow, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
        UiKit.card(root, card, this);
    }

    // ---- 颜色搭配：拖滑杆就行，不用查色号 ----

    /** 一条带渐变的色带滑杆，手指按哪儿就是哪儿 */
    private class HueBar {
        final FrameLayout box = new FrameLayout(AppearanceActivity.this);
        final View fill = new View(AppearanceActivity.this);
        final View knob = new View(AppearanceActivity.this);
        final int knobSize = UiKit.dp(AppearanceActivity.this, 24);
        int value = 0;
        UiKit.SliderCallback cb;

        HueBar(int initial, UiKit.SliderCallback callback) {
            cb = callback;
            value = initial;
            box.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, UiKit.dp(AppearanceActivity.this, 44)));

            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, UiKit.dp(AppearanceActivity.this, 16));
            flp.gravity = Gravity.CENTER_VERTICAL;
            flp.leftMargin = knobSize / 2;
            flp.rightMargin = knobSize / 2;
            fill.setLayoutParams(flp);
            fill.setBackground(gradient(0xFFFF0000, 0xFFFF0000));
            box.addView(fill);

            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(0xFFFFFFFF);
            circle.setStroke(UiKit.dp(AppearanceActivity.this, 2), 0x33000000);
            knob.setBackground(circle);
            FrameLayout.LayoutParams klp = new FrameLayout.LayoutParams(knobSize, knobSize);
            klp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            knob.setLayoutParams(klp);
            box.addView(knob);

            box.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent e) {
                    int a = e.getActionMasked();
                    if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE
                            || a == MotionEvent.ACTION_UP) {
                        float avail = box.getWidth() - knobSize;
                        if (avail > 0) {
                            float x = e.getX() - knobSize / 2f;
                            if (x < 0) {
                                x = 0;
                            }
                            if (x > avail) {
                                x = avail;
                            }
                            setValue(Math.round(x / avail * 100f), false);
                        }
                    }
                    if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                        if (cb != null) {
                            cb.onValue(value, true);
                        }
                    }
                    return true;
                }
            });
            box.post(new Runnable() {
                public void run() {
                    placeKnob();
                }
            });
        }

        private GradientDrawable gradient(int from, int to) {
            GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{from, to});
            g.setCornerRadius(UiKit.dp(AppearanceActivity.this, 8));
            return g;
        }

        void setBarColors(int[] colors) {
            GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            g.setCornerRadius(UiKit.dp(AppearanceActivity.this, 8));
            fill.setBackground(g);
        }

        void setValue(int v, boolean notify) {
            value = v < 0 ? 0 : (v > 100 ? 100 : v);
            placeKnob();
            if (notify && cb != null) {
                cb.onValue(value, true);
            } else if (!notify && cb != null) {
                cb.onValue(value, false);
            }
        }

        void placeKnob() {
            float avail = box.getWidth() - knobSize;
            if (avail <= 0) {
                return;
            }
            knob.setTranslationX(avail * (value / 100f));
        }
    }

    private void openColorPicker() {
        final float[] hsv = new float[3];
        int start = Store.parseColor(store.accentCustom());
        if (start == 0) {
            start = UiKit.ACCENT;
        }
        android.graphics.Color.colorToHSV(start, hsv);

        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 4), UiKit.dp(this, 16), UiKit.dp(this, 4));

        final View preview = new View(this);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiKit.dp(this, 44));
        plp.bottomMargin = UiKit.dp(this, 10);
        preview.setLayoutParams(plp);
        box.addView(preview);

        final TextView hexLabel = UiKit.label(this, "", 12.5f, 0xFF6B7076, false, Gravity.CENTER);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.bottomMargin = UiKit.dp(this, 8);
        box.addView(hexLabel, hlp);

        box.addView(tip("色相"));
        final HueBar hue = new HueBar(Math.round(hsv[0] / 3.6f), null);
        hue.setBarColors(new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                0xFF0000FF, 0xFFFF00FF, 0xFFFF0000});
        box.addView(hue.box);

        box.addView(tip("浓淡"));
        final HueBar sat = new HueBar(Math.round(hsv[1] * 100f), null);
        box.addView(sat.box);

        box.addView(tip("明暗"));
        final HueBar val = new HueBar(Math.round(hsv[2] * 100f), null);
        box.addView(val.box);

        final Runnable refresh = new Runnable() {
            public void run() {
                hsv[0] = hue.value * 3.6f;
                hsv[1] = sat.value / 100f;
                hsv[2] = Math.max(0.12f, val.value / 100f);
                int c = android.graphics.Color.HSVToColor(hsv);
                GradientDrawable g = new GradientDrawable();
                g.setCornerRadius(UiKit.dp(AppearanceActivity.this, 12));
                g.setColor(c);
                preview.setBackground(g);
                hexLabel.setText(Store.hexOf(c));
                float[] t = new float[]{hsv[0], 1f, 1f};
                int pure = android.graphics.Color.HSVToColor(t);
                sat.setBarColors(new int[]{0xFFBFC4CA, pure});
                val.setBarColors(new int[]{0xFF2A2C30, pure});
            }
        };

        UiKit.SliderCallback cb = new UiKit.SliderCallback() {
            public void onValue(int value, boolean done) {
                refresh.run();
            }
        };
        hookBar(hue, cb);
        hookBar(sat, cb);
        hookBar(val, cb);
        refresh.run();

        // 常用色，点一下就填进去，省得记色号
        LinearLayout quick = UiKit.row(this);
        final int[] swatches = {0xFF4F6BFF, 0xFF12A0B4, 0xFF2E9E5B, 0xFF7C5CFF, 0xFFE4801F,
                0xFFE0568A, 0xFF8A6A1F, 0xFF2F3238};
        for (int i = 0; i < swatches.length; i++) {
            final int c = swatches[i];
            View v = UiKit.dot(this, c, 28);
            v.setClickable(true);
            UiKit.pressable(v);
            v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    float[] t = new float[3];
                    android.graphics.Color.colorToHSV(c, t);
                    hue.setValue(Math.round(t[0] / 3.6f), false);
                    sat.setValue(Math.round(t[1] * 100f), false);
                    val.setValue(Math.round(t[2] * 100f), false);
                    refresh.run();
                }
            });
            quick.addView(v, UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 8, 0));
        }
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qlp.topMargin = UiKit.dp(this, 10);
        box.addView(quick, qlp);

        UiKit.dialog(this)
                .setTitle("调颜色")
                .setView(box)
                .setPositiveButton("用这个", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.setAccentCustom(hexLabel.getText().toString());
                        rebuildLater(true);
                    }
                })
                .setNeutralButton("回到预设", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.setAccentCustom("");
                        rebuildLater(true);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void hookBar(final HueBar bar, final UiKit.SliderCallback cb) {
        bar.cb = cb;
    }

    private TextView tip(String s) {
        TextView tv = UiKit.label(this, s, 12.5f, 0xFF6B7076, true, Gravity.START);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = UiKit.dp(this, 6);
        tv.setLayoutParams(p);
        return tv;
    }

    // ============ 背景 ============

    private void buildBackgroundCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "背景"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        String p = store.bgPath();
        card.addView(UiKit.label(this, p.isEmpty() ? "当前：没有设置背景图"
                        : ("当前：" + new java.io.File(p).getName()),
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        LinearLayout btns = UiKit.row(this);
        btns.addView(UiKit.smallButton(this, "选择背景图片", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        pickImage();
                    }
                }));
        btns.addView(UiKit.smallButton(this, "清除背景", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                store.setBgPath("");
                                rebuildLater(true);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        card.addView(btns, UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 16));

        card.addView(UiKit.sliderRow(this, "清晰度（越大越清楚，也更吃内存）", 30, 100,
                        store.bgClarity(), new UiKit.SliderCallback() {
                            public void onValue(int value, boolean done) {
                                if (!done) {
                                    return;
                                }
                                store.setBgClarity(value);
                                rebuildLater(false);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 14));

        card.addView(UiKit.sliderRow(this, "高斯模糊", 0, 40, store.bgBlur(),
                        new UiKit.SliderCallback() {
                            public void onValue(int value, boolean done) {
                                if (!done) {
                                    return;
                                }
                                store.setBgBlur(value);
                                rebuildLater(false);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, card, this);
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try {
            startActivityForResult(i, REQ_BG);
        } catch (Exception e) {
            Toast.makeText(this, "没有可用的图片选择器", Toast.LENGTH_SHORT).show();
        }
    }

    // ============ 磨砂玻璃 ============

    private void buildFrostCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "磨砂玻璃"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        card.addView(UiKit.label(this,
                        "打开之后卡片、输入框、顶栏都变成半透明的，底下那张背景图透上来。"
                                + "没有背景图的时候看不太出来",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        CheckBox box = new CheckBox(this);
        box.setText("磨砂玻璃 UI");
        box.setTextSize(13);
        box.setTextColor(UiKit.TEXT);
        box.setChecked(store.frosted());
        box.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.setFrosted(((CheckBox) v).isChecked());
                rebuildLater(true);
            }
        });
        card.addView(box, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));

        card.addView(UiKit.sliderRow(this, "磨砂玻璃的透明度", 0, 80, store.frostPercent(),
                        new UiKit.SliderCallback() {
                            public void onValue(int value, boolean done) {
                                if (!done) {
                                    return;
                                }
                                store.setFrostPercent(value);
                                if (!store.frosted()) {
                                    store.setFrosted(true);
                                }
                                rebuildLater(false);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, card, this);
    }

    // ============ 动画 ============

    private void buildAnimCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "界面动画"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        card.addView(UiKit.label(this,
                        "页面切换的滑动、卡片和消息的淡入、按钮按下的回弹都在这里管。"
                                + "觉得费电或者看着晕，可以关掉，关掉之后最省",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        CheckBox box = new CheckBox(this);
        box.setText("开启动效");
        box.setTextSize(13);
        box.setTextColor(UiKit.TEXT);
        box.setChecked(store.animOn());
        box.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.setAnimOn(((CheckBox) v).isChecked());
                UiKit.animationsOn = store.animOn();
                rebuildLater(true);
            }
        });
        card.addView(box);
        UiKit.card(root, card, this);
    }

    // ============ 开屏动画 ============

    private void buildSplashCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "开屏动画"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        card.addView(UiKit.label(this,
                        "启动时先亮一下图标，再淡进主界面。嫌等就直接关掉，关掉立刻进",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        CheckBox box = new CheckBox(this);
        box.setText("开启开屏动画");
        box.setTextSize(13);
        box.setTextColor(UiKit.TEXT);
        box.setChecked(store.splashOn());
        box.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.setSplashOn(((CheckBox) v).isChecked());
            }
        });
        card.addView(box);
        UiKit.card(root, card, this);
    }

    // ============ 字体 ============

    private void buildFontCard(LinearLayout root) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "字体"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        card.addView(UiKit.label(this,
                        "自定义字体只影响界面文字。选一个 ttf 或 otf 文件就能用，标题和正文一起换",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        card.addView(UiKit.label(this, store.fontPath().isEmpty()
                        ? "当前：系统默认字体"
                        : "当前：" + new java.io.File(store.fontPath()).getName(),
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        LinearLayout frow = UiKit.row(this);
        frow.addView(UiKit.smallButton(this, "选择字体文件", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        pickFont();
                    }
                }));
        frow.addView(UiKit.smallButton(this, "恢复默认字体", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                store.setFontPath("");
                                UiKit.loadFont("");
                                Toast.makeText(AppearanceActivity.this, "已恢复默认字体",
                                        Toast.LENGTH_SHORT).show();
                                rebuildLater(true);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        card.addView(frow);
        UiKit.card(root, card, this);
    }

    private void pickFont() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_FONT);
        } catch (Exception e) {
            Toast.makeText(this, "没有可用的文件选择器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        if (req == REQ_BG) {
            saveBackground(data);
        } else if (req == REQ_FONT) {
            saveFont(data);
        }
    }

    private void saveBackground(Intent data) {
        try {
            java.io.File dir = new java.io.File(getFilesDir(), "background");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            java.io.File out = new java.io.File(dir, "bg_" + System.currentTimeMillis() + ".img");
            java.io.InputStream is = getContentResolver().openInputStream(data.getData());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while (is != null && (n = is.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
            if (is != null) {
                is.close();
            }
            fos.close();
            store.setBgPath(out.getAbsolutePath());
            Toast.makeText(this, "已换成这张背景", Toast.LENGTH_SHORT).show();
            rebuildLater(true);
        } catch (Exception e) {
            Toast.makeText(this, "读图片失败：" + e.getClass().getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFont(Intent data) {
        try {
            java.io.File dir = new java.io.File(getFilesDir(), "fonts");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            java.io.File out = new java.io.File(dir, "font_" + System.currentTimeMillis() + ".ttf");
            java.io.InputStream is = getContentResolver().openInputStream(data.getData());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while (is != null && (n = is.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
            if (is != null) {
                is.close();
            }
            fos.close();
            store.setFontPath(out.getAbsolutePath());
            UiKit.loadFont(out.getAbsolutePath());
            if (!UiKit.hasCustomFont()) {
                store.setFontPath("");
                Toast.makeText(this, "这个文件读不出字体，换一个 ttf 或 otf 试试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, "已换字体", Toast.LENGTH_SHORT).show();
            rebuildLater(true);
        } catch (Exception e) {
            Toast.makeText(this, "读字体失败：" + e.getClass().getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
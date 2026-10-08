// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

/** 对话设置：聊天方式，以及只在气泡分段式下才用到的分段规则 */
public class ChatSettingsActivity extends Activity {

    private Store store;
    private ScrollView sv;
    private EditText segRegex;
    private EditText segCleanup;

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

    /** 换聊天方式时只重画内容，滚动位置留着 */
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
        top.addView(UiKit.label(this, "对话设置", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 18));

        // ============ 聊天方式 ============
        LinearLayout mode = UiKit.column(this);
        mode.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        mode.addView(UiKit.sectionTitle(this, "聊天方式"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        final String[][] modes = {
                {"气泡分段式", "回复按换行和标点切成几条气泡，一条一条放出来，中间隔两秒多。"
                        + "底下那套分段规则只有这个模式用得上"},
                {"普通式", "整条回复放在一个气泡里，一边写一边出，不切分，像别家聊天那样"}
        };
        for (int i = 0; i < modes.length; i++) {
            final int idx = i;
            boolean on = store.chatMode() == i;
            LinearLayout row = UiKit.column(this);
            row.setBackground(UiKit.shape(this, on ? UiKit.ACCENT_SOFT : 0x00000000,
                    on ? UiKit.ACCENT : UiKit.BORDER, 14));
            row.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12));

            LinearLayout head = UiKit.row(this);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(UiKit.label(this, modes[i][0], 14.5f, UiKit.TEXT, on, Gravity.START),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(UiKit.label(this, on ? "使用中" : "", 11.5f, UiKit.ACCENT, false, Gravity.CENTER));
            row.addView(head);
            row.addView(UiKit.label(this, modes[i][1], 11.5f, UiKit.TEXT_SUB, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    store.setChatMode(idx);
                    rebuildNow(true);
                }
            });
            UiKit.pressable(row);
            mode.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        }
        UiKit.card(root, mode, this);

        // ============ 分段设置：只在气泡分段式下出现 ============
        if (store.chatMode() == Store.CHAT_BUBBLE) {
            LinearLayout seg = UiKit.column(this);
            seg.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
            seg.addView(UiKit.sectionTitle(this, "分段设置"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 12));
            seg.addView(sub("回复先按换行分成多条气泡。某一段超过 60 字时，才用下面的正则再切；整条超过 800 字不分段"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 16));

            seg.addView(labelRowWithButton("分段正则表达式", "恢复默认", new View.OnClickListener() {
                public void onClick(View v) {
                    segRegex.setText(Store.DEF_SEG_REGEX);
                }
            }));
            segRegex = UiKit.field(this, Store.DEF_SEG_REGEX, store.segRegex());
            seg.addView(segRegex, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
            seg.addView(sub("默认 .*?[。？！!?~…；;]+|.+$，按句末标点切，标点保留（同 AstrBot 的 regex）"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 6, 0, 16));

            seg.addView(labelRowWithButton("内容过滤正则表达式", "恢复默认", new View.OnClickListener() {
                public void onClick(View v) {
                    segCleanup.setText(Store.DEF_SEG_CLEANUP);
                }
            }));
            segCleanup = UiKit.field(this, "留空表示不过滤", store.segCleanup());
            seg.addView(segCleanup, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
            seg.addView(sub("默认留空。填 [。？！] 会去掉段末的句末标点（同 AstrBot 的 content_cleanup_rule）"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 6, 0, 18));

            seg.addView(UiKit.button(this, "保存分段设置", 0xFFFFFFFF, UiKit.ACCENT, 12,
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    saveSeg();
                                }
                            }),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
            UiKit.card(root, seg, this);
        } else {
            LinearLayout tip = UiKit.column(this);
            tip.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
            tip.addView(UiKit.sectionTitle(this, "分段设置"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 10));
            TextView t = UiKit.label(this,
                    "现在是普通式，整条回复一个气泡，不分段，所以这里的分段规则暂时用不上。"
                            + "切回气泡分段式就能看到它",
                    12.5f, UiKit.TEXT_SUB, false, Gravity.START);
            tip.addView(t);
            UiKit.card(root, tip, this);
        }

        root.addView(UiKit.label(this,
                "换聊天方式不影响已经存下的对话，历史会按新方式重新画一遍",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 4, 16, 4, 0));
        return root;
    }

    private void saveSeg() {
        String rx = segRegex.getText().toString().trim();
        String cl = segCleanup.getText().toString().trim();
        String msg = "已保存";

        if (rx.isEmpty()) {
            rx = Store.DEF_SEG_REGEX;
            segRegex.setText(rx);
        } else if (!valid(rx)) {
            rx = Store.DEF_SEG_REGEX;
            segRegex.setText(rx);
            msg = "分段正则无效，已改回默认";
        }
        if (!cl.isEmpty() && !valid(cl)) {
            cl = "";
            segCleanup.setText("");
            msg = "内容过滤正则无效，已清空";
        }
        store.saveSegmentation(rx, cl);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean valid(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private LinearLayout labelRowWithButton(String name, String btn, View.OnClickListener l) {
        LinearLayout r = UiKit.row(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.addView(UiKit.label(this, name, 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(UiKit.smallButton(this, btn, UiKit.ACCENT, UiKit.ACCENT_SOFT, 10, l));
        return r;
    }

    private TextView sub(String s) {
        return UiKit.label(this, s, 11.5f, 0xFFA6ADB4, false, Gravity.START);
    }
}
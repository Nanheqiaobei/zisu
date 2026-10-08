// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** 语言设置：中文 / English / 跟随系统。切换后重建界面即刻生效 */
public class LanguageActivity extends Activity {

    private Store store;
    private LinearLayout listBox;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        UiKit.boot(this, store);

        LinearLayout root = UiKit.column(this);
        UiKit.paintTransparent(root);
        root.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 28));

        LinearLayout top = UiKit.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(UiKit.label(this, "语言", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 6));

        root.addView(UiKit.label(this, "界面语言只影响本软件的显示，不影响对话内容",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 16));

        listBox = UiKit.column(this);
        root.addView(listBox);

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);
        UiKit.pageIn(root);

        render();
    }

    private void render() {
        listBox.removeAllViews();
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));

        int cur = store.langMode();
        addRow(card, "跟随系统", -1, cur);
        addRow(card, "中文", Lang.ZH, cur);
        addRow(card, "English", Lang.EN, cur);

        UiKit.card(listBox, card, this);
    }

    /** 一行：左侧名称，右侧选中标记，点一下切换并重建 */
    private void addRow(LinearLayout card, String name, final int mode, int cur) {
        final boolean on = (cur == mode);
        LinearLayout row = UiKit.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 14), UiKit.dp(this, 14), UiKit.dp(this, 14));

        TextView t = UiKit.label(this, name, 14, UiKit.TEXT, on, Gravity.START);
        row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(UiKit.label(this, on ? "✓" : "", 15, UiKit.ACCENT, true, Gravity.END));

        row.setBackground(UiKit.shape(this, on ? UiKit.ACCENT_SOFT : UiKit.SURFACE,
                on ? UiKit.ACCENT : UiKit.BORDER, 12));
        row.setClickable(true);
        UiKit.pressable(row);
        row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.setLangMode(mode);
                recreate();
            }
        });
        card.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
    }
}
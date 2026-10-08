// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** 工具：AI 现在能用哪些工具、各是干什么的、开不开 */
public class ToolsActivity extends Activity {

    private Store store;

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
        top.addView(UiKit.label(this, "工具", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 14));

        root.addView(UiKit.label(this,
                "这里列的是它在这一轮对话里能伸手做的事。关掉的工具不会发给接口，"
                        + "它连名字都看不到，也就不会去用",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 6));

        Tools.Info[] all = Tools.all();
        for (int i = 0; i < all.length; i++) {
            final Tools.Info info = all[i];
            LinearLayout card = UiKit.column(this);
            card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));

            LinearLayout head = UiKit.row(this);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(UiKit.label(this, info.name, 15, UiKit.TEXT, true, Gravity.START),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            final CheckBox box = new CheckBox(this);
            box.setText(Lang.t("启用"));
            box.setTextSize(13);
            box.setTextColor(UiKit.TEXT);
            box.setChecked(store.toolEnabled(info.id));
            box.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    store.setToolEnabled(info.id, box.isChecked());
                    Toast.makeText(ToolsActivity.this,
                            box.isChecked() ? (Lang.t("已打开「") + info.name + "」")
                                    : (Lang.t("已关掉「") + info.name + "」"),
                            Toast.LENGTH_SHORT).show();
                }
            });
            head.addView(box);
            card.addView(head);

            card.addView(UiKit.label(this, info.desc, 12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 8, 0, 0));

            TextView note = UiKit.label(this, info.note, 11.5f, UiKit.NOTE_TEXT, false, Gravity.START);
            note.setBackground(UiKit.shape(this, UiKit.NOTE_BG, UiKit.NOTE_BORDER, 12));
            note.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 9), UiKit.dp(this, 12), UiKit.dp(this, 9));
            card.addView(note, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 0));

            UiKit.card(root, card, this);
        }

        // 自动看一眼：不是工具，是系统自己定时跑的那条路
        LinearLayout autoCard = UiKit.column(this);
        autoCard.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        LinearLayout autoHead = UiKit.row(this);
        autoHead.setGravity(Gravity.CENTER_VERTICAL);
        autoHead.addView(UiKit.label(this, "每几轮自动看一眼", 15, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final CheckBox autoBox = new CheckBox(this);
        autoBox.setText(Lang.t("启用"));
        autoBox.setTextSize(13);
        autoBox.setTextColor(UiKit.TEXT);
        autoBox.setChecked(store.selfAutoOn());
        autoBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.setSelfAutoOn(autoBox.isChecked());
                Toast.makeText(ToolsActivity.this,
                        autoBox.isChecked() ? Lang.t("已打开自动调整") : Lang.t("已关掉自动调整"),
                        Toast.LENGTH_SHORT).show();
            }
        });
        autoHead.addView(autoBox);
        autoCard.addView(autoHead);
        autoCard.addView(UiKit.label(this,
                        "这不是工具，是系统自己每 4 轮跑一次的设定检查：把最近对话交给它自己的元反思层，"
                                + "让它按证据判断改不改；要有具体理由它才会提方案，方案同样弹出来问你。",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 8, 0, 0));
        TextView autoNote = UiKit.label(this,
                "关掉之后，只有「修改自己的设定」这个工具还能让它改（那个也关了，设定就完全由你自己的操作决定）。"
                        + "它对你的了解仍然会从旧对话压出来的日志里长",
                11.5f, UiKit.NOTE_TEXT, false, Gravity.START);
        autoNote.setBackground(UiKit.shape(this, UiKit.NOTE_BG, UiKit.NOTE_BORDER, 12));
        autoNote.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 9), UiKit.dp(this, 12), UiKit.dp(this, 9));
        autoCard.addView(autoNote, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 0));
        UiKit.card(root, autoCard, this);

        root.addView(UiKit.label(this,
                "工具是它自己的手段，开不开由你定；但改成什么样，是它自己的元反思层的事，"
                        + "它只能提请求，这层没变",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 4, 16, 4, 0));

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);
        UiKit.pageIn(root);
    }
}
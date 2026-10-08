// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** 前代记录：完全重置时归档下来的对话，AI 读不到，使用者能翻 */
public class ArchiveActivity extends Activity {

    private Store store;
    private LinearLayout box;
    private final Set<Integer> open = new HashSet<Integer>();

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
        top.addView(UiKit.label(this, "前代记录", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 6));

        root.addView(UiKit.label(this,
                "完全重置之前的对话会存在这里。现在的它读不到，只有你能翻",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        box = UiKit.column(this);
        root.addView(box);

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
        box.removeAllViews();
        JSONArray a = store.archives();
        if (a.length() == 0) {
            LinearLayout empty = UiKit.column(this);
            empty.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 20), UiKit.dp(this, 16), UiKit.dp(this, 20));
            empty.addView(UiKit.label(this, "还没有前代记录", 13, UiKit.TEXT_SUB, false, Gravity.START));
            UiKit.card(box, empty, this);
            return;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (int i = a.length() - 1; i >= 0; i--) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) {
                continue;
            }
            final int index = i;
            JSONArray msgs = o.optJSONArray("messages");
            final int count = msgs == null ? 0 : msgs.length();
            final boolean isOpen = open.contains(i);

            LinearLayout card = UiKit.column(this);
            card.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 14));
            LinearLayout head = UiKit.row(this);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(UiKit.label(this,
                            ("compress".equals(o.optString("kind", "reset")) ? "压缩归档　" : "前代记录　")
                                    + fmt.format(new Date(o.optLong("time", 0))) + "　共 " + count + " 条",
                            13, UiKit.TEXT, true, Gravity.START),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(UiKit.smallButton(this, isOpen ? "收起" : "展开",
                    UiKit.ACCENT, UiKit.ACCENT_SOFT, 10, new View.OnClickListener() {
                        public void onClick(View v) {
                            if (isOpen) {
                                open.remove(index);
                            } else {
                                open.add(index);
                            }
                            render();
                        }
                    }));
            card.addView(head);

            if (isOpen && msgs != null) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < msgs.length(); j++) {
                    JSONObject m = msgs.optJSONObject(j);
                    if (m == null) {
                        continue;
                    }
                    sb.append("user".equals(m.optString("role", "")) ? Lang.t("你：") : Lang.t("它："))
                      .append(m.optString("content", "")).append("\n\n");
                }
                TextView t = UiKit.label(this, sb.toString().trim(), 12.5f, UiKit.TEXT, false, Gravity.START);
                t.setTextIsSelectable(true);
                card.addView(t, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));
            }

            card.addView(UiKit.outlineButton(this, "删掉这段记录", UiKit.DANGER, 0x33C0392B, 12,
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    confirmDelete(index);
                                }
                            }),
                    UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 0));
            UiKit.card(box, card, this);
        }
    }

    private void confirmDelete(final int index) {
        UiKit.dialog(this)
                .setTitle(Lang.t("删掉这段记录"))
                .setMessage(Lang.t("这段前代记录会被永久删除，不可恢复"))
                .setPositiveButton(Lang.t("删除"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.deleteArchiveAt(index);
                        open.clear();
                        render();
                        Toast.makeText(ArchiveActivity.this, Lang.t("已删除"), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }
}
// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 核心设定页：只有使用者能改，AI 只能读 */
public class CoreActivity extends Activity {

    private Store store;
    private EditText name;
    private EditText story;

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
        top.addView(UiKit.label(this, "核心设定", 20, UiKit.TEXT, true, Gravity.START),
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
                "只有一个名字，它只能读，不能改。行为那一部分归它自己维护",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 16));

        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));

        card.addView(UiKit.sectionTitle(this, "名字"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 6));
        name = UiKit.field(this, Store.DEF_NAME, store.coreName());
        name.setSingleLine(true);
        card.addView(name);

        card.addView(UiKit.label(this,
                        "只有一个名字。故事那一栏按你的要求删掉了，剩下的都归它自己维护",
                        11.5f, 0xFFA6ADB4, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 8, 0, 0));

        card.addView(UiKit.button(this, "保存", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                store.saveCore(name.getText().toString(), "");
                                Toast.makeText(CoreActivity.this, "已保存，改动已留痕",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 20, 0, 0));
        UiKit.card(root, card, this);

        JSONArray log = store.coreLog();
        if (log.length() > 0) {
            LinearLayout logCard = UiKit.column(this);
            logCard.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
            logCard.addView(UiKit.sectionTitle(this, "改动留痕"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 10));
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            StringBuilder sb = new StringBuilder();
            int from = Math.max(0, log.length() - 10);
            for (int i = log.length() - 1; i >= from; i--) {
                JSONObject o = log.optJSONObject(i);
                if (o == null) {
                    continue;
                }
                sb.append(fmt.format(new Date(o.optLong("time", 0))))
                  .append("　").append(o.optString("summary", "")).append("\n");
            }
            TextView t = UiKit.label(this, sb.toString().trim(), 12, UiKit.TEXT_SUB, false, Gravity.START);
            t.setTextIsSelectable(true);
            logCard.addView(t);
            UiKit.card(root, logCard, this);
        }

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);
        UiKit.pageIn(root);
    }
}
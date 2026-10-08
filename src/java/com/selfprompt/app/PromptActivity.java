// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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

public class PromptActivity extends Activity {

    private Store store;
    private LinearLayout box;
    private final Set<Integer> expanded = new HashSet<Integer>();

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
        top.addView(UiKit.label(this, "提示词", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.smallButton(this, "系统说明", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(PromptActivity.this, SystemInfoActivity.class));
                    }
                }));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }), UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 4));

        root.addView(UiKit.label(this,
                "它的设定只有一块，由它自己的元反思层每几轮拿最近对话回看一遍；要有具体理由才会提方案，方案先弹给你看，你点头才写进设定",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 4));

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

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        box.removeAllViews();
        renderCore();
        renderBlocks();
        renderVersions();

        LinearLayout act = UiKit.column(this);
        act.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        act.addView(UiKit.label(this,
                        "被改得不像样的时候，可以从这里回到出厂那一版。恢复也会记一条版本，回滚得回来",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));
        act.addView(UiKit.outlineButton(this, "恢复默认提示词", UiKit.DANGER, 0x33C0392B, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                confirmRestore();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        act.addView(UiKit.outlineButton(this, "恢复通用模板", UiKit.TEXT_SUB, UiKit.BORDER, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                confirmRestoreGeneric();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(box, act, this);
    }

    private void confirmRestore() {
        UiKit.dialog(this)
                .setTitle(Lang.t("恢复默认提示词"))
                .setMessage(Lang.t("当前这一版会被出厂设定替换，旧版本仍在历史里，随时能回滚。要继续吗"))
                .setPositiveButton(Lang.t("恢复"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.restoreDefaultSelf();
                        expanded.clear();
                        render();
                        Toast.makeText(PromptActivity.this, Lang.t("已恢复出厂设定"),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    /** 恢复通用模板：名字和设定一起换成中性版，去掉作者出厂角色的痕迹 */
    private void confirmRestoreGeneric() {
        UiKit.dialog(this)
                .setTitle(Lang.t("恢复通用模板"))
                .setMessage(Lang.t("名字会变成「") + Store.DEF_NAME_GENERIC + Lang.t("」，设定换成一个中性的通用起点，不含任何特定角色。当前版本仍在历史里，随时能回滚。要继续吗"))
                .setPositiveButton(Lang.t("恢复"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.restoreGenericSelf();
                        expanded.clear();
                        render();
                        Toast.makeText(PromptActivity.this, Lang.t("已恢复通用模板"),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    // ============ 核心层 ============

    private void renderCore() {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));

        LinearLayout head = UiKit.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(UiKit.sectionTitle(this, "核心设定 · 只有你能改"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(UiKit.smallButton(this, "编辑", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(PromptActivity.this, CoreActivity.class));
                    }
                }));
        card.addView(head);

        TextView name = UiKit.label(this, store.coreName(), 18, UiKit.TEXT, true, Gravity.START);
        card.addView(name, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));

        JSONArray log = store.coreLog();
        if (log.length() > 0) {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            int from = Math.max(0, log.length() - 3);
            for (int i = from; i < log.length(); i++) {
                JSONObject o = log.optJSONObject(i);
                if (o == null) {
                    continue;
                }
                sb.append(fmt.format(new Date(o.optLong("time", 0))))
                  .append("　").append(o.optString("summary", "")).append("\n");
            }
            card.addView(UiKit.label(this, "改动留痕：\n" + sb.toString().trim(),
                            11.5f, 0xFFA6ADB4, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 0));
        }
        UiKit.card(box, card, this);
    }

    // ============ AI 自己的块 ============

    private void renderBlocks() {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        card.addView(UiKit.sectionTitle(this, "它自己的设定 · 由系统维护"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 4));
        card.addView(UiKit.label(this, "你只能看、回滚、清理，不能编辑",
                        11.5f, 0xFFA6ADB4, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));

        String text = store.selfText();
        TextView body = UiKit.label(this, text.isEmpty() ? "（空）" : text,
                13.5f, text.isEmpty() ? 0xFFA6ADB4 : UiKit.TEXT, false, Gravity.START);
        body.setTypeface(Typeface.MONOSPACE);
        body.setTextIsSelectable(true);
        card.addView(body);
        UiKit.card(box, card, this);
    }

    private String renderBlockContent(JSONObject o) {
        String type = o.optString("type", "text");
        String c = o.optString("content", "");
        if ("list".equals(type)) {
            StringBuilder sb = new StringBuilder();
            String[] lines = c.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String s = lines[i].trim();
                if (!s.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append("· ").append(s);
                }
            }
            return sb.toString();
        }
        if ("kv".equals(type)) {
            StringBuilder sb = new StringBuilder();
            String[] lines = c.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String s = lines[i].trim();
                if (s.isEmpty()) {
                    continue;
                }
                int eq = s.indexOf('=');
                if (eq > 0) {
                    s = s.substring(0, eq).trim() + "：" + s.substring(eq + 1).trim();
                }
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(s);
            }
            return sb.toString();
        }
        return c;
    }

    // ============ 版本历史 ============

    private void renderVersions() {
        JSONArray v = store.aiVersions();
        final int n = v.length();

        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        LinearLayout head = UiKit.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(UiKit.sectionTitle(this, Lang.t("版本历史 · 共 ") + n + Lang.t(" 版")),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(UiKit.smallButton(this, "清理旧版本", UiKit.DANGER, 0x1FC0392B, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        confirmClean();
                    }
                }));
        card.addView(head, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 12));

        if (n == 0) {
            card.addView(UiKit.label(this, "还没有任何版本", 13, UiKit.TEXT_SUB, false, Gravity.START));
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (int i = n - 1; i >= 0; i--) {
            JSONObject o = v.optJSONObject(i);
            if (o == null) {
                continue;
            }
            final int index = i;
            final boolean current = (i == n - 1);
            final boolean open = expanded.contains(i);
            String source = o.optString("source", "ai");
            String src = "ai".equals(source) ? Lang.t("AI 自己改") : ("rollback".equals(source) ? Lang.t("你回滚")
                    : ("migrate".equals(source) ? Lang.t("旧版迁移") : Lang.t("系统")));

            LinearLayout item = UiKit.column(this);
            item.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12));

            LinearLayout h = UiKit.row(this);
            h.setGravity(Gravity.CENTER_VERTICAL);
            TextView pill = UiKit.label(this, current ? ("v" + (i + 1) + Lang.t(" · 当前")) : ("v" + (i + 1)),
                    11.5f, current ? 0xFFFFFFFF : UiKit.TEXT_SUB, true, Gravity.CENTER);
            pill.setBackground(UiKit.shape(this, current ? UiKit.ACCENT : UiKit.CHIP_BG, 0, 8));
            pill.setPadding(UiKit.dp(this, 9), UiKit.dp(this, 4), UiKit.dp(this, 9), UiKit.dp(this, 4));
            h.addView(pill);
            h.addView(UiKit.label(this, src + "　" + fmt.format(new Date(o.optLong("time", 0))),
                            11, 0xFFA6ADB4, false, Gravity.START),
                    UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, this, 8, 0, 8, 0));
            h.addView(UiKit.smallButton(this, open ? "收起" : "展开",
                    UiKit.ACCENT, UiKit.ACCENT_SOFT, 10, new View.OnClickListener() {
                        public void onClick(View v) {
                            if (open) {
                                expanded.remove(index);
                            } else {
                                expanded.add(index);
                            }
                            render();
                        }
                    }));
            item.addView(h);

            item.addView(UiKit.label(this, Lang.t("理由：") + o.optString("reason", ""),
                            12, UiKit.NOTE_TEXT, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));

            String summary = open ? versionText(o) : versionPreview(o);
            TextView body = UiKit.label(this, summary, 12.5f, UiKit.TEXT, false, Gravity.START);
            body.setTypeface(Typeface.MONOSPACE);
            body.setTextIsSelectable(true);
            item.addView(body, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));

            if (!current) {
                item.addView(UiKit.outlineButton(this, "回滚到这一版", UiKit.ACCENT, 0x552F6FED, 12,
                                new View.OnClickListener() {
                                    public void onClick(View v) {
                                        store.rollbackBlocksTo(index);
                                        expanded.clear();
                                        render();
                                        Toast.makeText(PromptActivity.this, Lang.t("已回滚"),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }),
                        UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 0));
            }

            item.setBackground(UiKit.shape(this, UiKit.INNER_BG, UiKit.BORDER, 12));
            card.addView(item, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        }
        UiKit.card(box, card, this);
    }

    private String versionText(JSONObject v) {
        JSONArray b = v.optJSONArray("blocks");
        if (b == null || b.length() == 0) {
            return "（这一版没有任何块）";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length(); i++) {
            JSONObject o = b.optJSONObject(i);
            if (o == null) {
                continue;
            }
            sb.append("## ").append(o.optString("title", "")).append("\n")
              .append(o.optString("content", "")).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String versionPreview(JSONObject v) {
        JSONArray b = v.optJSONArray("blocks");
        if (b == null || b.length() == 0) {
            return "（空）";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length(); i++) {
            JSONObject o = b.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String c = o.optString("content", "").replace("\n", " ");
            if (c.length() > 24) {
                c = c.substring(0, 24) + "…";
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(o.optString("title", "")).append("：").append(c);
        }
        String t = sb.toString();
        if (t.length() > 240) {
            t = t.substring(0, 240) + " …";
        }
        return t;
    }

    private void confirmClean() {
        int n = store.aiVersions().length();
        if (n <= 1) {
            Toast.makeText(this, Lang.t("只有当前这一版，不用清理"), Toast.LENGTH_SHORT).show();
            return;
        }
        UiKit.dialog(this)
                .setTitle(Lang.t("清理旧版本"))
                .setMessage(Lang.t("会删掉除当前版本以外的 ") + (n - 1) + Lang.t(" 个历史快照，当前的块不受影响。要继续吗"))
                .setPositiveButton(Lang.t("清理"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.keepOnlyCurrentAiVersion();
                        expanded.clear();
                        render();
                        Toast.makeText(PromptActivity.this, Lang.t("已清理"), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }
}
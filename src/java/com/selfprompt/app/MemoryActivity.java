// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
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

/** 压缩记忆页：只放对话压出来的日志，直接勾选直接删，不再有口令和质问 */
public class MemoryActivity extends Activity {

    private Store store;
    private LinearLayout box;
    private final Set<String> selected = new HashSet<String>();

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
        top.addView(UiKit.label(this, "压缩记忆", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.smallButton(this, "前代记录", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(MemoryActivity.this, ArchiveActivity.class));
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
                "这里是它长期记得的全部东西：对话攒到 40 条就会自动把最老的一段压成日志，放在下面。"
                        + "删掉一条就等于它不再记得那段事，原始对话挪在前代记录里，只有你能翻",
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

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        box.removeAllViews();

        final JSONArray all = store.memories();
        int total = 0;
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && !"superseded".equals(o.optString("status", "active"))) {
                total++;
            }
        }
        final boolean allSelected = total > 0 && selected.size() >= total;

        // 操作区
        LinearLayout ops = UiKit.column(this);
        ops.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 14));
        LinearLayout r1 = UiKit.row(this);
        r1.addView(UiKit.smallButton(this, allSelected ? "取消全选" : "全选",
                        UiKit.TEXT_SUB, UiKit.CHIP_BG, 10, new View.OnClickListener() {
                            public void onClick(View v) {
                                if (allSelected) {
                                    selected.clear();
                                } else {
                                    selectAll();
                                    return;
                                }
                                render();
                            }
                        }));
        r1.addView(UiKit.smallButton(this, Lang.t("删除所选（") + selected.size() + Lang.t("）"),
                        UiKit.DANGER, 0x1FC0392B, 10, new View.OnClickListener() {
                            public void onClick(View v) {
                                confirmDelete();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        ops.addView(r1);

        LinearLayout r2 = UiKit.row(this);
        r2.addView(UiKit.smallButton(this, "整库销毁", UiKit.DANGER, 0x1FC0392B, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        confirmDestroy();
                    }
                }));
        r2.addView(UiKit.smallButton(this, "完全重置", UiKit.DANGER, 0x1FC0392B, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                confirmFullReset();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        ops.addView(r2, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));
        UiKit.card(box, ops, this);

        // 列表
        LinearLayout list = UiKit.column(this);
        list.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 14));
        list.addView(UiKit.sectionTitle(this, Lang.t("压缩记忆 · 共 ") + total + Lang.t(" 条")),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));

        int shown = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (int i = all.length() - 1; i >= 0; i--) {
            final JSONObject o = all.optJSONObject(i);
            if (o == null || "superseded".equals(o.optString("status", "active"))) {
                continue;
            }
            shown++;
            final String id = o.optString("id", "");
            boolean legacy = !"log".equals(o.optString("kind", ""));

            LinearLayout row = UiKit.row(this);
            row.setGravity(Gravity.TOP);

            CheckBox cb = new CheckBox(this);
            cb.setChecked(selected.contains(id));
            cb.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        selected.add(id);
                    } else {
                        selected.remove(id);
                    }
                }
            });
            row.addView(cb);

            LinearLayout col = UiKit.column(this);
            col.addView(UiKit.label(this, o.optString("text", ""), 13.5f, UiKit.TEXT, false,
                    Gravity.START));
            col.addView(UiKit.label(this,
                            (legacy ? "早期条目 · " : "日志 · ")
                                    + fmt.format(new Date(o.optLong("created", 0))),
                            11, 0xFFA6ADB4, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 4, 0, 0));
            row.addView(col, UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, this, 6, 0, 0, 0));
            list.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 12));
        }
        if (shown == 0) {
            list.addView(UiKit.label(this,
                    "还没有压缩记忆。对话攒到 40 条以上，它会把最老的一段压成日志放在这里",
                    12.5f, 0xFFA6ADB4, false, Gravity.START));
        }
        UiKit.card(box, list, this);
    }

    private void selectAll() {
        selected.clear();
        JSONArray all = store.memories();
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && !"superseded".equals(o.optString("status", "active"))) {
                selected.add(o.optString("id", ""));
            }
        }
        render();
    }

    private void confirmDelete() {
        if (selected.isEmpty()) {
            Toast.makeText(this, Lang.t("先勾选要删的"), Toast.LENGTH_SHORT).show();
            return;
        }
        UiKit.dialog(this)
                .setTitle(Lang.t("删除 ") + selected.size() + Lang.t(" 条压缩记忆"))
                .setMessage(Lang.t("删掉就等于它不再记得这些事。原始对话还在前代记录里，你可以自己翻。不可恢复，要删吗"))
                .setPositiveButton(Lang.t("删除"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        JSONArray ids = new JSONArray();
                        for (String s : selected) {
                            ids.put(s);
                        }
                        int n = store.deleteMemoriesDirect(ids);
                        selected.clear();
                        render();
                        Toast.makeText(MemoryActivity.this, Lang.t("已删除 ") + n + Lang.t(" 条"),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void confirmDestroy() {
        final EditText input = UiKit.field(this, "手打：我确认销毁全部记忆", "");
        LinearLayout box1 = UiKit.column(this);
        box1.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 8), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box1.addView(input, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.dialog(this)
                .setTitle(Lang.t("整库销毁"))
                .setMessage(Lang.t("删掉全部压缩记忆，不可恢复。它不会知道发生过什么"))
                .setView(box1)
                .setPositiveButton(Lang.t("销毁"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (!Lang.t("我确认销毁全部记忆").equals(input.getText().toString().trim())) {
                            Toast.makeText(MemoryActivity.this, Lang.t("确认语不对，没有执行"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        store.destroyMemories();
                        selected.clear();
                        render();
                        Toast.makeText(MemoryActivity.this, Lang.t("已销毁，不可恢复"), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void confirmFullReset() {
        final EditText input = UiKit.field(this, "手打：我确认重置", "");
        LinearLayout box2 = UiKit.column(this);
        box2.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 8), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box2.addView(input, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.dialog(this)
                .setTitle(Lang.t("完全重置"))
                .setMessage(Lang.t("会清空记忆、把它的块恢复出厂，核心设定（名字和故事）保留，当前对话会归档成前代记录。不可恢复，要继续吗"))
                .setView(box2)
                .setPositiveButton(Lang.t("重置"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (!Lang.t("我确认重置").equals(input.getText().toString().trim())) {
                            Toast.makeText(MemoryActivity.this, Lang.t("确认语不对，没有执行"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        store.archiveMessages();
                        store.fullReset();
                        selected.clear();
                        Toast.makeText(MemoryActivity.this, Lang.t("已重置，它回到出厂"),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }
}
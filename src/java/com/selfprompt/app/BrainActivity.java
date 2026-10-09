// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 「脑」页：一个文件浏览 + 工作台。
 * 上半是统计与自定义区块，下半是脑文件夹的浏览，可点进去看文件、改结构。
 */
public class BrainActivity extends Activity {

    private Store store;
    private LinearLayout root;
    /** 当前浏览的目录，null 表示脑根 */
    private File curDir;
    /** 颞叶整理：一次调用是否在跑 */
    private boolean tidying = false;
    private ChatClient tidyClient;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        UiKit.boot(this, store);

        root = UiKit.column(this);
        UiKit.paintTransparent(root);
        root.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 28));

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从系统设置授权页回来，重新检查
        if (Brain.hasAccess(this) && curDir == null) {
            Brain.ensure(this);
        }
        refresh();
    }

    // ================= 主刷新 =================

    private void refresh() {
        root.removeAllViews();

        // ---- 顶栏 ----
        LinearLayout top = UiKit.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(UiKit.label(this, Lang.t("脑"), 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (Brain.hasAccess(this)) {
            top.addView(UiKit.smallButton(this, Lang.t("整理"), UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            runTidy();
                        }
                    }));
        }
        top.addView(UiKit.outlineButton(this, Lang.t("返回"), UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }), UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 12));

        // ---- 权限门 ----
        if (!Brain.hasAccess(this)) {
            buildPermGate();
            return;
        }

        File r = Brain.root(this);
        if (!r.exists() || !r.isDirectory()) {
            Brain.ensure(this);
        }
        MemLog.rebuildViews(this);
        // ---- 路径 + 统计 ----
        buildHeader(r);

        // ---- 自定义区块 ----
        buildBlocks(r);

        // ---- 文件浏览 ----
        buildBrowser(r);
    }

    // ================= 权限门 =================

    private View buildPermGate() {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18));
        card.addView(UiKit.label(this, Lang.t("还没有文件访问权限"), 15, UiKit.TEXT, true, Gravity.START));
        card.addView(UiKit.label(this,
                        Lang.t("「脑」要把记忆以文件形式存在本地，需要你授予「所有文件访问」权限。"
                                + "点下面的按钮，在系统设置里打开「自塑」的开关，再回到这里"),
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 8, 0, 14));
        card.addView(UiKit.button(this, Lang.t("授予文件访问权限"), 0xFFFFFFFF, UiKit.ACCENT, 12,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        requestAllFiles();
                    }
                }));
        UiKit.card(root, card, this);
        return card;
    }

    private void requestAllFiles() {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            } catch (Exception ignored) {
                Toast.makeText(this, Lang.t("打不开系统设置，请手动到设置里授权"), Toast.LENGTH_LONG).show();
            }
        }
    }

    // ================= 路径与统计 =================

    private View buildHeader(File r) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));

        LinearLayout row = UiKit.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(UiKit.label(this, Lang.t("位置"), 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(UiKit.smallButton(this, Lang.t("更改位置"), UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        pickFolderDialog(Brain.root(BrainActivity.this));
                    }
                }));
        card.addView(row);
        card.addView(UiKit.label(this, Brain.display(Brain.path(this)), 12.5f, UiKit.TEXT, false,
                        Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 12));

        Brain.Stat st = Brain.stat(r);
        String latest = st.latest > 0
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(st.latest))
                : Lang.t("无");
        card.addView(UiKit.label(this,
                        Lang.t("文件 ") + st.files + Lang.t(" 个 · 目录 ") + st.dirs
                                + Lang.t(" 个 · 共 ") + st.chars + Lang.t(" 字"),
                        12, UiKit.TEXT_SUB, false, Gravity.START));
        card.addView(UiKit.label(this, Lang.t("最近改动：") + latest, 12, UiKit.TEXT_SUB, false,
                        Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 4, 0, 0));
        UiKit.card(root, card, this);
        return card;
    }

    // ================= 自定义区块 =================

    private View buildBlocks(final File r) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));

        LinearLayout head = UiKit.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(UiKit.label(this, Lang.t("区块"), 15, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(UiKit.smallButton(this, Lang.t("添加区块"), UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        editBlockDialog(-1);
                    }
                }));
        card.addView(head);

        JSONArray blocks = Brain.blocks(this);
        if (blocks.length() == 0) {
            card.addView(UiKit.label(this,
                            Lang.t("还没有区块。区块可以把某个文件或目录的内容固定展示在这里，"
                                    + "比如「最近日记」「知识条目数」。点右上角添加"),
                            12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 8, 0, 0));
        }
        for (int i = 0; i < blocks.length(); i++) {
            final int idx = i;
            final JSONObject o = blocks.optJSONObject(i);
            if (o == null) {
                continue;
            }
            final String title = o.optString("title", "");
            final String path = o.optString("path", "");
            final String type = o.optString("type", "text");
            File f = Brain.resolve(this, path);

            LinearLayout blk = UiKit.column(this);
            blk.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 12), UiKit.dp(this, 14), UiKit.dp(this, 12));
            blk.setBackground(UiKit.shape(this, UiKit.INNER_BG, 0, 12));

            LinearLayout bh = UiKit.row(this);
            bh.setGravity(Gravity.CENTER_VERTICAL);
            bh.addView(UiKit.label(this, title.isEmpty() ? Lang.t("未命名") : title, 13.5f,
                            UiKit.TEXT, true, Gravity.START),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            bh.addView(UiKit.smallButton(this, Lang.t("改"), UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            editBlockDialog(idx);
                        }
                    }));
            bh.addView(UiKit.smallButton(this, Lang.t("删"), UiKit.DANGER, 0x1FC0392B, 10,
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    removeBlock(idx);
                                }
                            }),
                    UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 6, 0, 0, 0));
            blk.addView(bh);
            blk.addView(UiKit.label(this, Brain.display(path), 11, 0xFFA6ADB4, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 4, 0, 6));

            blk.addView(UiKit.label(this, renderBlock(f, type), 12.5f, UiKit.TEXT_SUB, false,
                    Gravity.START));
            card.addView(blk, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));
        }
        UiKit.card(root, card, this);
        return card;
    }

    /** 区块内容：text 取前若干行，count 统计，list 列目录 */
    private String renderBlock(File f, String type) {
        if (f == null || !f.exists()) {
            return Lang.t("（路径不存在）");
        }
        if ("count".equals(type)) {
            Brain.Stat st = Brain.stat(f);
            if (f.isDirectory()) {
                return Lang.t("目录：") + st.files + Lang.t(" 个文件 · 共 ") + st.chars + Lang.t(" 字");
            }
            return Lang.t("共 ") + Brain.readText(f).length() + Lang.t(" 字");
        }
        if ("list".equals(type)) {
            if (!f.isDirectory()) {
                return Lang.t("（不是目录）");
            }
            List<File> kids = Brain.listSorted(f);
            if (kids.isEmpty()) {
                return Lang.t("（空）");
            }
            StringBuilder sb = new StringBuilder();
            int n = Math.min(20, kids.size());
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append(kids.get(i).isDirectory() ? "▸ " : "· ").append(kids.get(i).getName());
            }
            if (kids.size() > n) {
                sb.append("\n…").append(Lang.t("共 ") ).append(kids.size()).append(Lang.t(" 项"));
            }
            return sb.toString();
        }
        // text
        String text = Brain.readText(f);
        if (text.isEmpty()) {
            return Lang.t("（空文件）");
        }
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        int n = Math.min(6, lines.length);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(lines[i]);
        }
        if (lines.length > n) {
            sb.append("\n…");
        }
        return sb.toString();
    }

    private void removeBlock(int idx) {
        JSONArray blocks = Brain.blocks(this);
        JSONArray out = new JSONArray();
        for (int i = 0; i < blocks.length(); i++) {
            if (i != idx) {
                out.put(blocks.optJSONObject(i));
            }
        }
        Brain.setBlocks(this, out);
        refresh();
    }

    private void editBlockDialog(final int idx) {
        final JSONArray blocks = Brain.blocks(this);
        JSONObject cur = idx >= 0 ? blocks.optJSONObject(idx) : null;
        final EditText titleIn = UiKit.field(this, Lang.t("区块名字"),
                cur == null ? "" : cur.optString("title", ""));
        final EditText pathIn = UiKit.field(this, Lang.t("路径（相对脑根，如 日记）"),
                cur == null ? "" : cur.optString("path", ""));
        final String[] types = {Lang.t("文本"), Lang.t("条目数"), Lang.t("列表")};
        final String[] typeKeys = {"text", "count", "list"};
        final int curType = cur == null ? 0 : indexOf(typeKeys, cur.optString("type", "text"));
        final int[] picked = {curType < 0 ? 0 : curType};

        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 4), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box.addView(titleIn, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
        box.addView(pathIn, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
        final TextView typeLbl = UiKit.label(this,
                Lang.t("展示方式：") + types[picked[0]], 12.5f, UiKit.TEXT_SUB, false, Gravity.START);
        box.addView(typeLbl);
        box.addView(UiKit.smallButton(this, Lang.t("切换展示方式"), UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                picked[0] = (picked[0] + 1) % types.length;
                                typeLbl.setText(Lang.t("展示方式：") + types[picked[0]]);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 8, 0, 0));

        UiKit.dialog(this)
                .setTitle(idx >= 0 ? Lang.t("编辑区块") : Lang.t("添加区块"))
                .setView(box)
                .setPositiveButton(Lang.t("保存"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        JSONObject o = Brain.makeBlock(
                                titleIn.getText().toString().trim(),
                                pathIn.getText().toString().trim(),
                                typeKeys[picked[0]]);
                        JSONArray out = new JSONArray();
                        if (idx >= 0) {
                            for (int i = 0; i < blocks.length(); i++) {
                                out.put(i == idx ? o : blocks.optJSONObject(i));
                            }
                        } else {
                            for (int i = 0; i < blocks.length(); i++) {
                                out.put(blocks.optJSONObject(i));
                            }
                            out.put(o);
                        }
                        Brain.setBlocks(BrainActivity.this, out);
                        refresh();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private static int indexOf(String[] a, String s) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(s)) {
                return i;
            }
        }
        return -1;
    }

    // ================= 文件浏览 =================

    private View buildBrowser(final File r) {
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));

        File dir = curDir != null && curDir.exists() ? curDir : r;

        LinearLayout head = UiKit.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(UiKit.label(this, Lang.t("浏览"), 15, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (!dir.equals(r)) {
            head.addView(UiKit.smallButton(this, Lang.t("上一级"), UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            curDir = dir.getParentFile();
                            if (curDir != null && !curDir.getAbsolutePath().startsWith(r.getAbsolutePath())) {
                                curDir = r;
                            }
                            refresh();
                        }
                    }));
        }
        head.addView(UiKit.smallButton(this, Lang.t("新建文件夹"), UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                newFolderDialog(dir);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 6, 0, 0, 0));
        card.addView(head);

        card.addView(UiKit.label(this, relative(r, dir), 11.5f, 0xFFA6ADB4, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 10));

        List<File> kids = Brain.listSorted(dir);
        if (kids.isEmpty()) {
            card.addView(UiKit.label(this, Lang.t("（这个目录还是空的）"), 12.5f, 0xFFA6ADB4,
                    false, Gravity.START));
        }
        for (int i = 0; i < kids.size(); i++) {
            final File f = kids.get(i);
            LinearLayout row = UiKit.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(UiKit.shape(this, UiKit.CHIP_BG, 0, 12));
            row.setPadding(UiKit.dp(this, 12), UiKit.dp(this, 11), UiKit.dp(this, 12), UiKit.dp(this, 11));
            row.addView(UiKit.label(this, f.isDirectory() ? "▸" : "·", 14, UiKit.ACCENT, true,
                    Gravity.CENTER));
            row.addView(UiKit.label(this, f.getName(), 13.5f, UiKit.TEXT, false, Gravity.START),
                    UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, this, 10, 0, 0, 0));
            row.addView(UiKit.label(this, f.isDirectory() ? "" : sizeText(f), 11, UiKit.TEXT_SUB,
                    false, Gravity.CENTER));
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (f.isDirectory()) {
                        curDir = f;
                        refresh();
                    } else {
                        viewFileDialog(f);
                    }
                }
            });
            row.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    fileMenu(f);
                    return true;
                }
            });
            UiKit.pressable(row);
            card.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
        }
        UiKit.card(root, card, this);
        return card;
    }

    private String relative(File r, File dir) {
        String rp = r.getAbsolutePath();
        String dp = dir.getAbsolutePath();
        if (dp.equals(rp)) {
            return Brain.display(rp);
        }
        if (dp.startsWith(rp)) {
            return Brain.display(rp) + dp.substring(rp.length());
        }
        return Brain.display(dp);
    }

    private String sizeText(File f) {
        int n = Brain.readText(f).length();
        return n + Lang.t(" 字");
    }

    private void newFolderDialog(final File dir) {
        final EditText input = UiKit.field(this, Lang.t("文件夹名"), "");
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 4), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box.addView(input);
        UiKit.dialog(this)
                .setTitle(Lang.t("新建文件夹"))
                .setView(box)
                .setPositiveButton(Lang.t("创建"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        String n = input.getText().toString().trim();
                        if (n.isEmpty()) {
                            return;
                        }
                        File f = new File(dir, n);
                        if (!f.exists() && !f.mkdirs()) {
                            Toast.makeText(BrainActivity.this, Lang.t("创建失败"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        refresh();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void fileMenu(final File f) {
        final String[] items = {Lang.t("重命名"), Lang.t("删除"), Lang.t("新建同名文本追加内容")};
        new AlertDialog.Builder(this)
                .setTitle(f.getName())
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            renameDialog(f);
                        } else if (which == 1) {
                            confirmDelete(f);
                        } else {
                            appendDialog(f);
                        }
                    }
                })
                .show();
    }

    private void renameDialog(final File f) {
        final EditText input = UiKit.field(this, Lang.t("新名字"), f.getName());
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 4), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box.addView(input);
        UiKit.dialog(this)
                .setTitle(Lang.t("重命名"))
                .setView(box)
                .setPositiveButton(Lang.t("保存"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        String n = input.getText().toString().trim();
                        if (n.isEmpty()) {
                            return;
                        }
                        File dst = new File(f.getParentFile(), n);
                        if (!f.renameTo(dst)) {
                            Toast.makeText(BrainActivity.this, Lang.t("重命名失败"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        refresh();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void confirmDelete(final File f) {
        UiKit.dialog(this)
                .setTitle(Lang.t("删除"))
                .setMessage(f.isDirectory()
                        ? Lang.t("这是目录，里面的东西会一起删掉。不可恢复，要删吗")
                        : Lang.t("删掉就没了，要删吗"))
                .setPositiveButton(Lang.t("删除"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        Brain.deleteRecursive(f);
                        refresh();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void appendDialog(final File f) {
        final EditText input = UiKit.field(this, Lang.t("要追加的内容"), "");
        input.setMinLines(5);
        input.setGravity(Gravity.TOP | Gravity.START);
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 4), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box.addView(input);
        UiKit.dialog(this)
                .setTitle(Lang.t("追加内容"))
                .setView(box)
                .setPositiveButton(Lang.t("追加"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        Brain.appendText(f, input.getText().toString().trim());
                        refresh();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void viewFileDialog(final File f) {
        String text = Brain.readText(f);
        if (text.isEmpty()) {
            text = Lang.t("（空文件）");
        }
        final TextView tv = UiKit.label(this, text, 13, UiKit.TEXT, false, Gravity.START);
        tv.setTextIsSelectable(true);
        ScrollView sv = new ScrollView(this);
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 6), UiKit.dp(this, 16), UiKit.dp(this, 6));
        box.addView(tv);
        sv.addView(box);
        UiKit.dialog(this)
                .setTitle(f.getName())
                .setView(sv)
                .setPositiveButton(Lang.t("关闭"), null)
                .show();
    }

    // ================= 目录选择器 =================

    private void pickFolderDialog(final File start) {
        File dir = start != null && start.isDirectory() ? start : new File("/sdcard");
        if (!dir.exists()) {
            dir = Environment.getExternalStorageDirectory();
        }
        final File cur = dir;
        final List<File> kids = Brain.listSorted(cur);
        final java.util.List<File> dirs = new java.util.ArrayList<File>();
        final java.util.List<String> names = new java.util.ArrayList<String>();
        names.add(Lang.t("✔ 选定当前目录：") + Brain.display(cur.getAbsolutePath()));
        for (int i = 0; i < kids.size(); i++) {
            if (kids.get(i).isDirectory()) {
                dirs.add(kids.get(i));
                names.add("▸ " + kids.get(i).getName());
            }
        }
        final String[] arr = names.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(Brain.display(cur.getAbsolutePath()))
                .setItems(arr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            setBrainPath(cur);
                        } else {
                            File next = dirs.get(which - 1);
                            pickFolderDialog(next);
                        }
                    }
                })
                .setNeutralButton(Lang.t("上一级"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        File up = cur.getParentFile();
                        if (up != null) {
                            pickFolderDialog(up);
                        }
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void setBrainPath(File dir) {
        Brain.setPath(this, dir.getAbsolutePath());
        curDir = null;
        if (Brain.ensure(this)) {
            Toast.makeText(this, Lang.t("已把「脑」设到 ") + Brain.display(dir.getAbsolutePath()),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, Lang.t("这个位置写不进去，换一个试试"), Toast.LENGTH_LONG).show();
        }
        refresh();
    }

    // ================= 颞叶：整理 =================
    /** 把「结论层」现有内容 + 「观察流」的新原始观察交给颞叶，让它重写结论层 */
private void runTidy() {
        if (tidying) {
            Toast.makeText(this, Lang.t("正在整理，稍等"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (store.apiKey().isEmpty()) {
            Toast.makeText(this, Lang.t("接口还没配好"), Toast.LENGTH_SHORT).show();
            return;
        }
        String pending = MemLog.pendingDigest(this, 80);
        String digest = MemLog.digest(this, 120, 120);
        if (pending.trim().isEmpty() && digest.trim().isEmpty()) {
            Toast.makeText(this, Lang.t("「脑」里还没有可整理的内容"), Toast.LENGTH_SHORT).show();
            return;
        }
        JSONArray req = new JSONArray();
        try {
            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content", store.fnPromptOrDefault(Store.FN_TEMPORAL));
            req.put(sys);
            JSONObject u = new JSONObject();
            u.put("role", "user");
            u.put("content", "「待分类」：\n" + (pending.trim().isEmpty() ? "（无）" : pending)
                    + "\n\n「记忆流现有条目」：\n" + (digest.trim().isEmpty() ? "（空）" : digest));
            req.put(u);
        } catch (Exception e) {
            return;
        }
        tidying = true;
        Toast.makeText(this, Lang.t("颞叶开始整理…"), Toast.LENGTH_SHORT).show();
        final StringBuilder out = new StringBuilder();
        final int idx = store.pickProfile(Store.FN_TEMPORAL);
        tidyClient = new ChatClient();
        tidyClient.send(store.baseUrlOf(idx), store.apiKeyOf(idx), store.modelOf(idx), req,
                new ChatClient.Listener() {
                    public void onDelta(String t) {
                        out.append(t);
                    }
                    public void onToolCall(String id, String name, String args) {
                    }
                    public void onReasoning(String text) {
                    }

                    public void onDone(final String error) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                onTidyDone(error, out.toString());
                            }
                        });
                    }
                }, store.tempOf(idx), store.topPOf(idx), store.maxTokensOf(idx),
                null, false, "none");
    }
    private void onTidyDone(String error, String text) {
        tidying = false;
        if (error != null) {
            Toast.makeText(this, Lang.t("整理失败：") + error, Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject jo = Store.extractJson(text);
        if (jo == null) {
            Toast.makeText(this, Lang.t("颞叶没给出可用的结果"), Toast.LENGTH_SHORT).show();
            return;
        }
        int[] r = MemLog.applyTemporal(this, jo);
        if (r == null) {
            Toast.makeText(this, Lang.t("颞叶没给出可用的结果"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (r[0] == 0 && r[1] == 0) {
            Toast.makeText(this, Lang.t("整理完成，没有发现重复"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, Lang.t("整理完成：归档 ") + r[0] + Lang.t(" 条，合并 ") + r[1] + Lang.t(" 组"), Toast.LENGTH_LONG).show();
        }
        refresh();
    }
    /** 归一化：去空白与常见标点，转小写，用于粗略比对 */
    private static String norm(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase().replaceAll(
                "[\\s，。！？、,.!?~～:：;；\"'“”‘’()（）\\[\\]【】#\\-*·◆]", "");
    }
}
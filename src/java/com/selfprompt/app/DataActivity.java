// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** 数据：备份、还原、单独导出配置、单独导出角色。导出为 zip，连附件头像背景一起带走 */
public class DataActivity extends Activity {

    private static final int REQ_EXPORT = 4001;
    private static final int REQ_IMPORT = 4002;

    private Store store;
    private String pendingExportType = Store.EXPORT_FULL;

    /** 私有目录里要打包的子目录：配置类、角色类 */
    private static final String[] CONFIG_DIRS = {"avatars", "background", "fonts"};
    private static final String[] CHAR_DIRS = {"attach"};

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
        top.addView(UiKit.label(this, "数据", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 18));

        // ---- 备份与还原 ----
        LinearLayout backup = UiKit.column(this);
        backup.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
        backup.addView(UiKit.sectionTitle(this, "备份与还原"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        backup.addView(UiKit.label(this,
                        "备份导出一个 zip，里面是所有设置、对话、记忆，还有附件、头像、背景图和字体。"
                                + "还原时拿这份文件盖回去，会把当前数据整个换掉，先备份再还原",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));
        backup.addView(UiKit.button(this, "备份数据（导出全部）", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startExport(Store.EXPORT_FULL, "自塑备份");
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        backup.addView(UiKit.outlineButton(this, "数据还原（从备份文件恢复）", UiKit.TEXT_SUB,
                        UiKit.BORDER, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                confirmImport();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, backup, this);

        // ---- 单独导出 ----
        LinearLayout single = UiKit.column(this);
        single.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
        single.addView(UiKit.sectionTitle(this, "单独导出"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        single.addView(UiKit.label(this,
                        "想把配置和角色分开带走时用这个。配置包括接口、key、外观与界面设置（含背景图与字体）；"
                                + "角色包括它的设定与提示词、压缩记忆、前代记录、对话（含图片附件），以及功能模型的提示词",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));
        single.addView(UiKit.button(this, "单独导出配置数据", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startExport(Store.EXPORT_CONFIG, "自塑配置");
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        single.addView(UiKit.button(this, "单独导出角色", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startExport(Store.EXPORT_CHARACTER, "自塑角色");
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, single, this);

        // ---- 清空对话 ----
        LinearLayout clear = UiKit.column(this);
        clear.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
        clear.addView(UiKit.sectionTitle(this, "清空对话"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        clear.addView(UiKit.label(this, "只删当前对话，提示词版本、记忆和配置都保留",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));
        clear.addView(UiKit.outlineButton(this, "清空对话记录", UiKit.DANGER, 0x33C0392B, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                confirmClear();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, clear, this);

        root.addView(UiKit.label(this,
                "导出的 zip 里含 API key，别随手发出去。还原后要重启应用才全部生效",
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

    // ============ 导出 ============

    private void startExport(String type, String baseName) {
        pendingExportType = type;
        String name = baseName + "_" + stamp() + ".zip";
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE, name);
        try {
            startActivityForResult(i, REQ_EXPORT);
        } catch (Exception e) {
            Toast.makeText(this, Lang.t("这台机器没有可用的文件保存入口"), Toast.LENGTH_SHORT).show();
        }
    }

    private String stamp() {
        java.text.SimpleDateFormat f =
                new java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.CHINA);
        return f.format(new java.util.Date());
    }

    /** 把 prefs 的 JSON 与相关的私有目录文件一起打成 zip */
    private void writeExport(Uri uri) {
        ZipOutputStream zos = null;
        try {
            JSONObject data = store.exportData(pendingExportType);
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) {
                throw new Exception(Lang.t("打不开目标文件"));
            }
            zos = new ZipOutputStream(os);
            zos.putNextEntry(new ZipEntry("data.json"));
            zos.write(data.toString(2).getBytes("UTF-8"));
            zos.closeEntry();

            List<String> dirs = new ArrayList<String>();
            if (Store.EXPORT_FULL.equals(pendingExportType)) {
                for (String d : CONFIG_DIRS) {
                    dirs.add(d);
                }
                for (String d : CHAR_DIRS) {
                    dirs.add(d);
                }
            } else if (Store.EXPORT_CONFIG.equals(pendingExportType)) {
                for (String d : CONFIG_DIRS) {
                    dirs.add(d);
                }
            } else {
                for (String d : CHAR_DIRS) {
                    dirs.add(d);
                }
            }
            int count = 0;
            for (int i = 0; i < dirs.size(); i++) {
                count += zipDir(zos, new File(getFilesDir(), dirs.get(i)), "files/" + dirs.get(i));
            }
            zos.finish();
            zos.close();
            zos = null;
            Toast.makeText(this, Lang.t("已导出（含 ") + count + Lang.t(" 个文件）"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, Lang.t("导出失败：") + e.getClass().getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private int zipDir(ZipOutputStream zos, File dir, String prefix) throws Exception {
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        File[] fs = dir.listFiles();
        if (fs == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < fs.length; i++) {
            File f = fs[i];
            if (f.isDirectory()) {
                n += zipDir(zos, f, prefix + "/" + f.getName());
                continue;
            }
            zos.putNextEntry(new ZipEntry(prefix + "/" + f.getName()));
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[8192];
            int r;
            while ((r = fis.read(buf)) > 0) {
                zos.write(buf, 0, r);
            }
            fis.close();
            zos.closeEntry();
            n++;
        }
        return n;
    }

    // ============ 导入 ============

    private void confirmImport() {
        UiKit.dialog(this)
                .setTitle(Lang.t("数据还原"))
                .setMessage(Lang.t("选一份备份文件恢复。文件里若是全部数据，会把当前数据整个换成它；")
                        + Lang.t("若是单类（配置 / 角色），只覆盖对应那一类。不可撤销，要继续吗"))
                .setPositiveButton(Lang.t("去选文件"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        pickImport();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void pickImport() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_IMPORT);
        } catch (Exception e) {
            Toast.makeText(this, Lang.t("这台机器没有可用的文件选择入口"), Toast.LENGTH_SHORT).show();
        }
    }

    private void doImport(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                throw new Exception(Lang.t("打不开这个文件"));
            }
            ZipInputStream zis = new ZipInputStream(is);
            final JSONObject data = new JSONObject();
            final List<String> names = new ArrayList<String>();
            final List<byte[]> blobs = new ArrayList<byte[]>();
            boolean hasData = false;
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (e.isDirectory()) {
                    continue;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int r;
                while ((r = zis.read(buf)) > 0) {
                    bos.write(buf, 0, r);
                }
                zis.closeEntry();
                String name = e.getName();
                if ("data.json".equals(name)) {
                    JSONObject o = new JSONObject(new String(bos.toByteArray(), "UTF-8"));
                    java.util.Iterator<String> it = o.keys();
                    while (it.hasNext()) {
                        String k = it.next();
                        data.put(k, o.opt(k));
                    }
                    hasData = true;
                } else if (name.startsWith("files/")) {
                    names.add(name);
                    blobs.add(bos.toByteArray());
                }
            }
            zis.close();
            if (!hasData) {
                Toast.makeText(this, Lang.t("这不是自塑导出的文件"), Toast.LENGTH_SHORT).show();
                return;
            }
            String type = data.optString("type", Store.EXPORT_FULL);
            String tName = Store.EXPORT_CONFIG.equals(type) ? "配置"
                    : (Store.EXPORT_CHARACTER.equals(type) ? "角色" : "全部");
            final String fType = type;
            final List<String> fNames = names;
            final List<byte[]> fBlobs = blobs;
            UiKit.dialog(this)
                    .setTitle(Lang.t("确认还原"))
                    .setMessage(Lang.t("这份文件是「") + tName + Lang.t("」类，含 ") + names.size()
                            + Lang.t(" 个文件，将覆盖对应的数据。确定写入吗"))
                    .setPositiveButton(Lang.t("还原"), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int which) {
                            applyImport(fType, data, fNames, fBlobs);
                        }
                    })
                    .setNegativeButton(Lang.t("取消"), null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, Lang.t("读取失败：") + e.getClass().getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void applyImport(String type, JSONObject data, List<String> names, List<byte[]> blobs) {
        try {
            int c = store.importData(data);
            // 文件：先清掉该类的旧文件，再写回新的
            List<String> dirs = new ArrayList<String>();
            if (Store.EXPORT_FULL.equals(type)) {
                for (String d : CONFIG_DIRS) {
                    dirs.add(d);
                }
                for (String d : CHAR_DIRS) {
                    dirs.add(d);
                }
            } else if (Store.EXPORT_CONFIG.equals(type)) {
                for (String d : CONFIG_DIRS) {
                    dirs.add(d);
                }
            } else {
                for (String d : CHAR_DIRS) {
                    dirs.add(d);
                }
            }
            for (int i = 0; i < dirs.size(); i++) {
                deleteDir(new File(getFilesDir(), dirs.get(i)));
            }
            int fc = 0;
            for (int i = 0; i < names.size(); i++) {
                String rel = names.get(i).substring("files/".length());
                File out = new File(getFilesDir(), rel);
                File parent = out.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(out);
                fos.write(blobs.get(i));
                fos.close();
                fc++;
            }
            Toast.makeText(this, Lang.t("已还原 ") + c + Lang.t(" 项、") + fc + Lang.t(" 个文件，重启后全部生效"),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, Lang.t("还原失败：") + e.getClass().getSimpleName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            if (fs != null) {
                for (int i = 0; i < fs.length; i++) {
                    deleteDir(fs[i]);
                }
            }
        }
        dir.delete();
    }

    private void confirmClear() {
        UiKit.dialog(this)
                .setTitle(Lang.t("清空对话记录"))
                .setMessage(Lang.t("对话记录会被删除，提示词版本、记忆和配置都会保留。要继续吗"))
                .setPositiveButton(Lang.t("清空"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.clearMessages();
                        Toast.makeText(DataActivity.this, Lang.t("已清空"), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        if (req == REQ_EXPORT) {
            writeExport(data.getData());
        } else if (req == REQ_IMPORT) {
            doImport(data.getData());
        }
    }
}
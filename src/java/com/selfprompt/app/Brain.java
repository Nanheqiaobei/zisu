// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 「脑」：本地记忆文件夹。默认 /sdcard/塑脑。
 * 记忆以纯文本文件形式存在本地，软件只做浏览与工作台，不把它们藏进私有目录。
 */
public class Brain {

    private static final String PREF = "selfprompt";
    private static final String K_PATH = "brain_path";
    private static final String K_BLOCKS = "brain_blocks";

    /** 六个大分支，建库时自动生成 */
    public static final String[] BRANCHES = {"自我", "人物", "知识", "日记", "系统"};

    public static String defaultPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/塑脑";
    }

    public static String path(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String p = sp.getString(K_PATH, "");
        if (p == null || p.trim().isEmpty()) {
            return defaultPath();
        }
        return p.trim();
    }

    public static void setPath(Context c, String p) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(K_PATH, p == null ? "" : p.trim()).apply();
    }

    public static File root(Context c) {
        return new File(path(c));
    }

    /** 界面上展示用：把 /storage/emulated/0 显示成 /sdcard */
    public static String display(String p) {
        if (p == null) {
            return "";
        }
        String ext = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (p.startsWith(ext)) {
            return "/sdcard" + p.substring(ext.length());
        }
        return p;
    }

    /** 是否具备读写外部存储的权限 */
    public static boolean hasAccess(Context c) {
        if (Build.VERSION.SDK_INT >= 30) {
            return Environment.isExternalStorageManager();
        }
        return c.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /** 建好目录结构与说明文件，已存在则不动 */
    public static boolean ensure(Context c) {
        try {
            File r = root(c);
            if (!r.exists()) {
                r.mkdirs();
            }
            if (!r.isDirectory()) {
                return false;
            }
            for (int i = 0; i < BRANCHES.length; i++) {
                File d = new File(r, BRANCHES[i]);
                if (!d.exists()) {
                    d.mkdirs();
                }
            }
            File readme = new File(r, "说明.md");
            if (!readme.exists()) {
                writeText(readme, DEFAULT_README);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static final String DEFAULT_README =
            "# 脑\n"
            + "\n"
            + "这是「自塑」的长期记忆存放处。\n"
            + "\n"
            + "## 结构\n"
            + "- 系统/记忆流.jsonl   记忆本体：一行一条 JSON，只增不改\n"
            + "- 各分支下的 .md 文件是「视图」：由记忆流机械生成，给人看的，随时可重建\n"
            + "- 日记/    它写的日记，按日期命名，只增不删\n"
            + "\n"
            + "## 说明\n"
            + "- 要改记忆：告诉它，或直接编辑 记忆流.jsonl（改坏了删掉那一行即可）\n"
            + "- 视图文件不要手动编辑，下一次生成会覆盖\n"
            + "- 拿不准的，先问，不要编造\n";
    // ---- 文件读写 ----

    public static String readText(File f) {
        if (f == null || !f.isFile()) {
            return "";
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (!first) {
                    sb.append("\n");
                }
                sb.append(line);
                first = false;
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean writeText(File f, String text) {
        if (f == null) {
            return false;
        }
        try {
            File p = f.getParentFile();
            if (p != null && !p.exists()) {
                p.mkdirs();
            }
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            w.write(text == null ? "" : text);
            w.flush();
            w.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean appendText(File f, String text) {
        if (f == null) {
            return false;
        }
        try {
            File p = f.getParentFile();
            if (p != null && !p.exists()) {
                p.mkdirs();
            }
            boolean exists = f.exists();
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
            if (exists && f.length() > 0 && text != null && !text.startsWith("\n")) {
                w.write("\n");
            }
            w.write(text == null ? "" : text);
            w.flush();
            w.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean deleteRecursive(File f) {
        if (f == null || !f.exists()) {
            return false;
        }
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) {
                for (int i = 0; i < kids.length; i++) {
                    deleteRecursive(kids[i]);
                }
            }
        }
        return f.delete();
    }

    /** 目录内容：文件夹在前、文件在后，各自按名字排 */
    public static List<File> listSorted(File dir) {
        List<File> out = new ArrayList<File>();
        if (dir == null || !dir.isDirectory()) {
            return out;
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            return out;
        }
        for (int i = 0; i < kids.length; i++) {
            String n = kids[i].getName();
            if (n.startsWith(".")) {
                continue;
            }
            out.add(kids[i]);
        }
        Collections.sort(out, new Comparator<File>() {
            public int compare(File a, File b) {
                boolean da = a.isDirectory();
                boolean db = b.isDirectory();
                if (da != db) {
                    return da ? -1 : 1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
        return out;
    }

    // ---- 统计 ----

    public static class Stat {
        public int files;
        public int dirs;
        public int chars;
        public long latest;
    }

    public static Stat stat(File dir) {
        Stat s = new Stat();
        walk(dir, s);
        return s;
    }

    private static void walk(File f, Stat s) {
        if (f == null || !f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            s.dirs++;
            File[] kids = f.listFiles();
            if (kids != null) {
                for (int i = 0; i < kids.length; i++) {
                    if (kids[i].getName().startsWith(".")) {
                        continue;
                    }
                    walk(kids[i], s);
                }
            }
        } else {
            s.files++;
            s.chars += readText(f).length();
            if (f.lastModified() > s.latest) {
                s.latest = f.lastModified();
            }
        }
    }

    // ---- 自定义区块 ----

    /** 区块：title 名字，path 相对脑根或绝对路径，type 展示方式（text / list / count） */
    public static JSONArray blocks(Context c) {
        try {
            return new JSONArray(c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                    .getString(K_BLOCKS, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static void setBlocks(Context c, JSONArray a) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(K_BLOCKS, a == null ? "[]" : a.toString()).apply();
    }

    public static JSONObject makeBlock(String title, String path, String type) {
        JSONObject o = new JSONObject();
        try {
            o.put("title", title == null ? "" : title);
            o.put("path", path == null ? "" : path);
            o.put("type", type == null ? "text" : type);
        } catch (Exception ignored) {
        }
        return o;
    }

    /** 把区块里的 path 解析成绝对文件：绝对路径原样，相对路径挂到脑根下 */
    public static File resolve(Context c, String p) {
        if (p == null || p.trim().isEmpty()) {
            return root(c);
        }
        String s = p.trim();
        File f = new File(s);
        if (f.isAbsolute()) {
            return f;
        }
        return new File(root(c), s);
    }
}

// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。
package com.selfprompt.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 「记忆流」：统一数据层。
 * 一切记忆都是一条只增不改的条目，存在 脑/系统/记忆流.jsonl（一行一条 JSON）。
 * 「脑」里的文件是视图：由条目机械生成，给人看的，随时可重建。
 * 合并判决（verdict）也是条目：追加进流，可追溯、可撤销。
 *
 * 条目字段：id / ts / kind / branch / text / tags / src
 * 判决字段：id / ts / kind=verdict / keep / drop / reason
 */
public class MemLog {
    public static final String K_VERDICT = "verdict";

    public static File file(Context c) {
        return new File(new File(Brain.root(c), "系统"), "记忆流.jsonl");
    }

    /** 逐行读所有条目；坏行跳过 */
    public static JSONArray readAll(Context c) {
        JSONArray arr = new JSONArray();
        File f = file(c);
        if (!f.isFile()) {
            return arr;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    arr.put(new JSONObject(t));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception ignored) {
            }
        }
        return arr;
    }

    public static void append(Context c, JSONObject o) {
        File f = file(c);
        File p = f.getParentFile();
        if (p != null && !p.exists()) {
            p.mkdirs();
        }
        try {
            boolean exists = f.exists();
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
            if (exists && f.length() > 0) {
                w.write("\n");
            }
            w.write(o.toString());
            w.flush();
            w.close();
        } catch (Exception ignored) {
        }
    }

    public static int nextId(Context c) {
        JSONArray all = readAll(c);
        int max = 0;
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            int id = o == null ? 0 : o.optInt("id", 0);
            if (id > max) {
                max = id;
            }
        }
        return max + 1;
    }

    /** 判决后的有效条目（非判决、未被废弃），按写入顺序 */
    public static JSONArray effective(Context c) {
        JSONArray all = readAll(c);
        Set<Integer> dead = new HashSet<Integer>();
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o == null || !K_VERDICT.equals(o.optString("kind", ""))) {
                continue;
            }
            JSONArray drop = o.optJSONArray("drop");
            if (drop != null) {
                for (int j = 0; j < drop.length(); j++) {
                    dead.add(drop.optInt(j, -1));
                }
            }
        }
        JSONArray out = new JSONArray();
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o == null || K_VERDICT.equals(o.optString("kind", ""))) {
                continue;
            }
            if (dead.contains(o.optInt("id", -1))) {
                continue;
            }
            out.put(o);
        }
        return out;
    }

    public static JSONObject byId(Context c, int id) {
        JSONArray all = readAll(c);
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && o.optInt("id", -1) == id) {
                return o;
            }
        }
        return null;
    }

    public static boolean hasId(Context c, int id) {
        return byId(c, id) != null;
    }

    /** 最近 n 条有效条目 */
    public static JSONArray recent(Context c, int n) {
        JSONArray eff = effective(c);
        JSONArray out = new JSONArray();
        int from = Math.max(0, eff.length() - n);
        for (int i = from; i < eff.length(); i++) {
            out.put(eff.optJSONObject(i));
        }
        return out;
    }

    /** 给模型看的条目清单：编号 [分支/类别] 正文（截断） */
    public static String digest(Context c, int maxItems, int maxChars) {
        JSONArray eff = effective(c);
        StringBuilder sb = new StringBuilder();
        int from = Math.max(0, eff.length() - maxItems);
        for (int i = from; i < eff.length(); i++) {
            JSONObject o = eff.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String t = o.optString("text", "");
            if (t.length() > maxChars) {
                t = t.substring(0, maxChars) + "…";
            }
            sb.append("#").append(o.optInt("id", 0)).append(" [")
              .append(o.optString("branch", "")).append("/").append(o.optString("kind", "")).append("] ")
              .append(t).append("\n");
        }
        return sb.toString();
    }

    /** 全部有效条目 text 拼接，用于程序级去重比对 */
    public static String allText(Context c) {
        JSONArray eff = effective(c);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eff.length(); i++) {
            JSONObject o = eff.optJSONObject(i);
            if (o != null) {
                sb.append(o.optString("text", "")).append("\n");
            }
        }
        return sb.toString();
    }

    /** 某分支的有效条目 text 拼接 */
    public static String branchText(Context c, String branch) {
        JSONArray eff = effective(c);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < eff.length(); i++) {
            JSONObject o = eff.optJSONObject(i);
            if (o != null && branch.equals(o.optString("branch", ""))) {
                sb.append(o.optString("text", "")).append("\n");
            }
        }
        return sb.toString();
    }

    /** 归一化：去空白与常见标点，转小写，用于粗略比对 */
    public static String norm(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase().replaceAll(
                "[\\s，。！？、,.!?~～:：;；\"'“”‘’()（）\\[\\]【】#\\-*·◆]", "");
    }

    // ================= 待分类缓冲 =================
    /** 海马体提炼出来、还没经颞叶归档的原始信息 */
    public static File pendingFile(Context c) {
        return new File(new File(Brain.root(c), "系统"), "待分类.jsonl");
    }

    public static JSONArray readPending(Context c) {
        JSONArray arr = new JSONArray();
        File f = pendingFile(c);
        if (!f.isFile()) {
            return arr;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    arr.put(new JSONObject(t));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception ignored) {
            }
        }
        return arr;
    }

    public static void appendPending(Context c, String ts, String text) {
        File f = pendingFile(c);
        File p = f.getParentFile();
        if (p != null && !p.exists()) {
            p.mkdirs();
        }
        try {
            boolean exists = f.exists();
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
            if (exists && f.length() > 0) {
                w.write("\n");
            }
            JSONObject o = new JSONObject();
            o.put("ts", ts);
            o.put("text", text);
            w.write(o.toString());
            w.flush();
            w.close();
        } catch (Exception ignored) {
        }
    }

    public static void clearPending(Context c) {
        File f = pendingFile(c);
        try {
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception ignored) {
        }
    }

    /** 待分类条目的清单文本，给颞叶看 */
    public static String pendingDigest(Context c, int max) {
        JSONArray arr = readPending(c);
        StringBuilder sb = new StringBuilder();
        int from = Math.max(0, arr.length() - max);
        for (int i = from; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String t = o.optString("text", "");
            if (t.length() > 120) {
                t = t.substring(0, 120) + "…";
            }
            sb.append("- ").append(t).append("\n");
        }
        return sb.toString();
    }

    /** 应用颞叶输出：归档新条目 + 应用合并判决 + 清空待分类。返回 {新增, 合并组数}；null 表示格式不对，未处理 */
    public static int[] applyTemporal(Context c, JSONObject jo) {
        if (jo == null || !jo.has("entries")) {
            return null;
        }
        int added = 0;
        JSONArray arr = jo.optJSONArray("entries");
        if (arr != null && arr.length() > 0) {
            String existing = norm(allText(c));
            int nextId = nextId(c);
            String stamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",
                    java.util.Locale.CHINA).format(new java.util.Date());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject e = arr.optJSONObject(i);
                if (e == null) {
                    continue;
                }
                String t = e.optString("text", "").trim();
                if (t.isEmpty()) {
                    continue;
                }
                String nt = norm(t);
                if (nt.length() >= 3 && existing.contains(nt)) {
                    continue;
                }
                String branch = e.optString("branch", "知识").trim();
                if (!branch.equals("自我") && !branch.equals("人物") && !branch.equals("知识") && !branch.equals("系统")) {
                    branch = "知识";
                }
                String kind = e.optString("kind", "事实").trim();
                if (kind.isEmpty()) {
                    kind = "事实";
                }
                existing += nt;
                try {
                    JSONObject o = new JSONObject();
                    o.put("id", nextId++);
                    o.put("ts", stamp);
                    o.put("branch", branch);
                    o.put("kind", kind);
                    o.put("text", t);
                    JSONArray tg = e.optJSONArray("tags");
                    if (tg != null && tg.length() > 0) {
                        o.put("tags", tg);
                    }
                    append(c, o);
                    added++;
                } catch (Exception ignored) {
                }
            }
        }
        int merged = applyMerges(c, jo.optJSONArray("merges"));
        clearPending(c);
        if (added > 0 && merged == 0) {
            rebuildViews(c);
        }
        return new int[]{added, merged};
    }

    /** 应用颞叶的合并判决：把判决本身作为条目追加进流，然后重建视图。返回实际应用的组数 */
    public static int applyMerges(Context c, JSONArray merges) {
        if (merges == null || merges.length() == 0) {
            return 0;
        }
        int applied = 0;
        for (int i = 0; i < merges.length(); i++) {
            JSONObject m = merges.optJSONObject(i);
            if (m == null) {
                continue;
            }
            int keep = m.optInt("keep", -1);
            JSONArray drop = m.optJSONArray("drop");
            if (keep <= 0 || drop == null || drop.length() == 0) {
                continue;
            }
            // keep 和 drop 都必须是流里真实存在的条目
            if (!hasId(c, keep)) {
                continue;
            }
            JSONArray cleanDrop = new JSONArray();
            for (int j = 0; j < drop.length(); j++) {
                int d = drop.optInt(j, -1);
                if (d > 0 && d != keep && hasId(c, d)) {
                    cleanDrop.put(d);
                }
            }
            if (cleanDrop.length() == 0) {
                continue;
            }
            try {
                JSONObject o = new JSONObject();
                o.put("id", nextId(c));
                o.put("ts", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
                        .format(new java.util.Date()));
                o.put("kind", K_VERDICT);
                o.put("keep", keep);
                o.put("drop", cleanDrop);
                o.put("reason", m.optString("reason", ""));
                append(c, o);
                applied++;
            } catch (Exception ignored) {
            }
        }
        if (applied > 0) {
            rebuildViews(c);
        }
        return applied;
    }

    /** 重建全部视图。机械生成，不调模型，零漂移 */
    public static void rebuildViews(Context c) {
        JSONArray eff = effective(c);
        List<JSONObject> people = new ArrayList<JSONObject>();
        List<JSONObject> notes = new ArrayList<JSONObject>();
        List<JSONObject> logs = new ArrayList<JSONObject>();
        List<JSONObject> self = new ArrayList<JSONObject>();
        List<JSONObject> flow = new ArrayList<JSONObject>();
        for (int i = 0; i < eff.length(); i++) {
            JSONObject o = eff.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String branch = o.optString("branch", "");
            String kind = o.optString("kind", "");
            if ("人物".equals(branch)) {
                people.add(o);
            } else if ("自我".equals(branch)) {
                self.add(o);
            } else if ("系统".equals(branch)) {
                flow.add(o);
            } else if ("日志".equals(kind)) {
                logs.add(o);
            } else {
                notes.add(o);
            }
        }
        writeView(c, "人物/档案.md",
                "# 人物档案\n\n（本文件由「记忆流」机械生成，不要手动编辑；要改内容，告诉它）\n",
                people, false);
        writeView(c, "知识/笔记.md",
                "# 笔记\n\n（本文件由「记忆流」机械生成，不要手动编辑）\n",
                notes, false);
        writeView(c, "知识/日志.md",
                "# 日志\n\n（本文件由「记忆流」机械生成，不要手动编辑）\n",
                logs, true);
        writeView(c, "自我/认识.md",
                "# 自我认识\n\n（本文件由「记忆流」机械生成，不要手动编辑）\n",
                self, false);
        writeView(c, "系统/流程.md",
                "# 流程\n\n（本文件由「记忆流」机械生成，不要手动编辑）\n",
                flow, false);
    }

    private static void writeView(Context c, String rel, String head, List<JSONObject> items, boolean byDay) {
        StringBuilder sb = new StringBuilder(head);
        if (byDay) {
            String lastDay = null;
            for (int i = 0; i < items.size(); i++) {
                JSONObject o = items.get(i);
                String ts = o.optString("ts", "");
                String day = ts.length() >= 10 ? ts.substring(0, 10) : ts;
                if (!day.equals(lastDay)) {
                    lastDay = day;
                    sb.append("\n## ").append(day).append("\n");
                }
                sb.append(line(o));
            }
        } else {
            for (int i = 0; i < items.size(); i++) {
                sb.append(line(items.get(i)));
            }
        }
        Brain.writeText(new File(Brain.root(c), rel), sb.toString());
    }

    private static String line(JSONObject o) {
        StringBuilder sb = new StringBuilder("- ");
        JSONArray tags = o.optJSONArray("tags");
        if (tags != null && tags.length() > 0) {
            sb.append("[");
            for (int i = 0; i < tags.length(); i++) {
                if (i > 0) {
                    sb.append("·");
                }
                sb.append(tags.optString(i, ""));
            }
            sb.append("] ");
        }
        sb.append(o.optString("text", ""));
        sb.append("（").append(o.optString("ts", "")).append(" · #").append(o.optInt("id", 0)).append("）\n");
        return sb.toString();
    }
}

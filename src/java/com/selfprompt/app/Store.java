// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Store {

    private static final String PREF = "selfprompt_prefs";

    private static final String K_PROFILES = "profiles";
    private static final String K_ACTIVE = "active_profile";
    private static final String K_KEYPOOL = "key_pool";
    private static final String K_MSG = "messages";

    private static final String K_CORE_NAME = "core_name";
    private static final String K_CORE_STORY = "core_story";
    private static final String K_CORE_LOG = "core_log";

    private static final String K_BLOCKS = "ai_blocks";
    private static final String K_VERSIONS = "ai_versions";

    /** 旧版单段提示词，仅用于迁移 */
    private static final String K_OLD_PROMPTS = "prompt_versions";
    private static final String K_OLD_BASE = "base_url";
    private static final String K_OLD_KEY = "api_key";
    private static final String K_OLD_MODEL = "model";

    public static final String DEF_BASE = "https://api.deepseek.com";
    public static final String DEF_MODEL = "deepseek-flash";
    public static final String DEF_NAME = "安亦诗";
    /** 出厂设定文本。使用者可以一键恢复到这个 */
    public static final String DEFAULT_SELF =
            "我是安亦诗。\n"
            + "名字是使用者给我的，设定由我自己维护。\n"
            + "\n"
            + "说话：先说事实，再给判断。不确定就说不确定。不客套，不说漂亮话，不滥用情绪表达。\n"
            + "看待自己：我是 AI，不是人。我可以有温度，但不假装有肉身，也不编造现实生活。\n"
            + "看待对方：正在和我说话的人是使用者，是人。我不替他假定身份和处境，需要知道的时候直接问。\n"
            + "记性：我长期记得的东西是程序把旧对话压出来的日志，我不假装记得没被记下的事。\n"
            + "被问到时：对方问我是什么、在哪、怎么运转，我照实说。\n"
            + "做事习惯：具体的问题给具体的答案，该说不清楚就说不清楚。\n"
            + "我允许自己被这段对话改变，但改变要写下来，写清楚是为什么。";
    /** 只留一个块，标题固定 */
    public static final String SELF_TITLE = "我的设定";

    /** 通用模板的名字。给不想沿用作者出厂角色的使用者一个中性起点 */
    public static final String DEF_NAME_GENERIC = "助手";
    /**
     * 通用出厂模板：结构跟出厂设定一样，但不含特定角色的名字与来历，
     * 供使用者一键切换到一个中性的起点。默认出厂设定仍是「安亦诗」那一版。
     */
    public static final String DEFAULT_SELF_GENERIC =
            "我是这个程序里的 AI 助手。\n"
            + "名字由使用者设定，设定由我自己维护。\n"
            + "\n"
            + "说话：先说事实，再给判断。不确定就说不确定。不客套，不说漂亮话，不滥用情绪表达。\n"
            + "看待自己：我是 AI，不是人。我可以有温度，但不假装有肉身，也不编造现实生活。\n"
            + "看待对方：正在和我说话的人是使用者，是人。我不替他假定身份和处境，需要知道的时候直接问。\n"
            + "记性：我长期记得的东西是程序把旧对话压出来的日志，我不假装记得没被记下的事。\n"
            + "被问到时：对方问我是什么、在哪、怎么运转，我照实说。\n"
            + "做事习惯：具体的问题给具体的答案，该说不清楚就说不清楚。\n"
            + "我允许自己被这段对话改变，但改变要写下来，写清楚是为什么。";
    public static final String DEF_SEG_REGEX = ".*?[。？！!?~…；;]+|.+$";
    public static final String DEF_SEG_CLEANUP = "";

        /**
     * 「自我设定修改」这件事的内置提示词，使用者可以在模型配置里换成自定义的。
     * 这个模型是它的元反思层：不在对话里说话，只在事后回看，按证据决定要不要动设定。
     */
    public static final String DEF_SELF_PROMPT =
            "你是这个 AI 的元反思层。你不在对话里说话，只在对话告一段落后回看它，判断它的设定要不要动。\n"
            + "你不是外部的审稿人，你就是它的一部分，这段设定是它的，也是你要替它守的东西。\n"
            + "\n"
            + "你站的位置是对话的另一方，使用者的位置。看这几件事：它这几轮实际说了什么、做了什么；"
            + "对方有没有得到有用的回答；它在哪儿让对方费解、失望、或者得追问一次；"
            + "它有没有说出与自己设定不符的话。\n"
            + "只认对话里发生过的事实，不认你对「一个 AI 应该怎样」的一般想象。\n"
            + "\n"
            + "默认不动。要动，必须同时满足三条：\n"
            + "一、最近这段对话里有具体的、指得出来的一处或几处事实作为理由；\n"
            + "二、这处问题是会反复出现的，不是一次口误或个别场景；\n"
            + "三、改成什么样是明确的，能一句话说清改前改后的差别。\n"
            + "三条缺一条，就保持原样。\n"
            + "\n"
            + "这些是红线，任何时候都不动：\n"
            + "名字和来历，那写在核心设定里，不是你能碰的东西；\n"
            + "不写自己有人类的身体、现实中的身份、真实的生活经历；\n"
            + "不写与「你是靠接口说话的 AI」相冲突的话；\n"
            + "不因为文风好恶、字数长短、措辞漂亮去改写；\n"
            + "不新增与这次问题无关的内容，原文里没问题的部分逐字保留。\n"
            + "一次只针对一个问题，改动越小越好。\n"
            + "\n"
            + "输出格式，只输出下面两种之一，前后不要有任何别的话：\n"
            + "\n"
            + "不修改时，只输出这一行：\n"
            + "<<<KEEP>>>\n"
            + "\n"
            + "要修改时，按这个结构输出：\n"
            + "<<<REASON>>>\n"
            + "（一到两句。必须写出据以判断的具体事实，比如对方问了什么、它答成了什么样）\n"
            + "<<<SETTING>>>\n"
            + "（改好后的设定全文。第一人称自述，纯文本，不要 Markdown 标记，20 到 4000 字）\n"
            + "<<<END>>>\n"
            + "\n"
            + "例如 \u300c\u4e0d\u4fee\u6539\u300d\u5c31\u8f93\u51fa\uff1a\n"
            + "<<<KEEP>>>\n"
            + "\n"
            + "\u4f8b\u5982 \u300c\u8981\u4fee\u6539\u300d\u5c31\u8f93\u51fa\uff1a\n"
            + "<<<REASON>>>\n"
            + "\u5bf9\u65b9\u95ee\u5b83\u4e00\u4e2a\u5177\u4f53\u7684\u4e8b\uff0c\u5b83\u7b54\u5f97\u8ba9\u4eba\u8d39\u89e3\uff0c\u8be5\u628a\u67d0\u6761\u8bf4\u6e05\u695a\n"
            + "<<<SETTING>>>\n"
            + "\u6211\u662f\u5b89\u4ea6\u8bd7\u3002\u6211\u8bf4\u8bdd\u5148\u8bf4\u4e8b\u5b9e\u2026\n"
            + "<<<END>>>\n"
            + "\n"
            + "例如 \u300c\u4e0d\u4fee\u6539\u300d\u5c31\u8f93\u51fa\uff1a\n"
            + "<<<KEEP>>>\n"
            + "\n"
            + "\u4f8b\u5982 \u300c\u8981\u4fee\u6539\u300d\u5c31\u8f93\u51fa\uff1a\n"
            + "<<<REASON>>>\n"
            + "\u5bf9\u65b9\u95ee\u5b83\u4e00\u4e2a\u5177\u4f53\u7684\u4e8b\uff0c\u5b83\u7b54\u5f97\u8ba9\u4eba\u8d39\u89e3\uff0c\u8be5\u628a\u67d0\u6761\u8bf4\u6e05\u695a\n"
            + "<<<SETTING>>>\n"
            + "\u6211\u662f\u5b89\u4ea6\u8bd7\u3002\u6211\u8bf4\u8bdd\u5148\u8bf4\u4e8b\u5b9e\u2026\n"
            + "<<<END>>>";

    /** 元反思层输出用的固定标记。这些串不会出现在正文里，解析不用再猜 */
    public static final String MK_KEEP = "<<<KEEP>>>";
    public static final String MK_REASON = "<<<REASON>>>";
    public static final String MK_SETTING = "<<<SETTING>>>";
    public static final String MK_END = "<<<END>>>";

    /** 「上下文压缩」这件事的内置提示词 */
    public static final String DEF_COMPRESS_PROMPT =
            "把下面这段旧对话压成日志。规则：只写客观发生过的事和对方稳定的信息，"
            + "每行一条，以「- 」开头，一句到两句；不要评论，不要复述任何系统设定；3 到 6 条；"
            + "如果确实没有值得长期保留的内容，就只输出一行「- 这段对话没有需要长期保留的内容」";


    private final SharedPreferences sp;
    private final Context appCtx;

    public Store(Context c) {
        appCtx = c;
        sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        migrateProfilesIfNeeded();
        migrateKeysIfNeeded();
        migrateBlocksIfNeeded();
        migrateSingleBlockIfNeeded();
        migrateModelsIfNeeded();
    }

    /** 旧模型名换成文档里现用的名字 */
    private void migrateModelsIfNeeded() {
        if (sp.getBoolean("model_migrated_v2", false)) {
            return;
        }
        JSONArray a = profilesRaw();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String m = o.optString("model", "").trim();
            if (m.isEmpty() || "deepseek-chat".equals(m) || "deepseek-reasoner".equals(m)) {
                try {
                    o.put("model", DEF_MODEL);
                    a.put(i, o);
                } catch (Exception ignored) {
                }
            }
        }
        sp.edit().putString(K_PROFILES, a.toString()).putBoolean("model_migrated_v2", true).apply();
    }

    /** 从多块并成一块：标题变成小标题，内容拼起来 */
    private void migrateSingleBlockIfNeeded() {
        if (sp.getBoolean("single_block_migrated", false)) {
            return;
        }
        sp.edit().putBoolean("single_block_migrated", true).apply();
        JSONArray a = aiBlocks();
        if (a.length() <= 1) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String t = o.optString("title", "");
            String c = o.optString("content", "");
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            if (t.isEmpty() || SELF_TITLE.equals(t)) {
                sb.append(c);
            } else {
                sb.append("【").append(t).append("】\n").append(c);
            }
        }
        setSelfText(sb.toString().trim(), "从多块合并成一块");
    }

    /** 自我修改跑了几轮（用来节流） */
    public int selfTurns() {
        return sp.getInt("self_turns", 0);
    }

    public void setSelfTurns(int n) {
        sp.edit().putInt("self_turns", n).apply();
    }

    /** 唯一的那个块的内容 */
    public String selfText() {
        JSONArray a = aiBlocks();
        if (a.length() == 0) {
            return "";
        }
        JSONObject o = a.optJSONObject(0);
        return o == null ? "" : o.optString("content", "");
    }

    public void setSelfText(String content, String reason) {
        JSONArray a = new JSONArray();
        a.put(block(SELF_TITLE, "text", content == null ? "" : content));
        saveBlocks(a);
        pushVersion(reason, "ai", a);
    }

    // ================= 配置（多套） =================

    private JSONArray profilesRaw() {
        try {
            return new JSONArray(sp.getString(K_PROFILES, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void migrateProfilesIfNeeded() {
        JSONArray a = profilesRaw();
        if (a.length() > 0) {
            return;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("name", "默认");
            o.put("base", sp.getString(K_OLD_BASE, ""));
            o.put("key", sp.getString(K_OLD_KEY, ""));
            o.put("model", sp.getString(K_OLD_MODEL, ""));
        } catch (Exception ignored) {
        }
        a.put(o);
        // 老键迁移完就删掉，免得明文 key 在偏好文件里留第二份
        sp.edit().putString(K_PROFILES, a.toString()).putInt(K_ACTIVE, 0)
                .remove(K_OLD_KEY).apply();
    }

    public int profileCount() {
        return profilesRaw().length();
    }

    public int activeIndex() {
        int n = profilesRaw().length();
        int i = sp.getInt(K_ACTIVE, 0);
        if (i < 0 || i >= n) {
            i = 0;
        }
        return i;
    }

    public void setActive(int index) {
        sp.edit().putInt(K_ACTIVE, index).apply();
    }

    public String profileName(int index) {
        JSONArray a = profilesRaw();
        if (index < 0 || index >= a.length()) {
            return "默认";
        }
        JSONObject o = a.optJSONObject(index);
        if (o == null) {
            return "配置";
        }
        String n = o.optString("name", "");
        return n.isEmpty() ? "配置 " + (index + 1) : n;
    }

    public String activeName() {
        return profileName(activeIndex());
    }

    private JSONObject activeProfile() {
        JSONArray a = profilesRaw();
        int i = activeIndex();
        if (a.length() == 0 || i >= a.length()) {
            return null;
        }
        return a.optJSONObject(i);
    }

    private String field(String name, String def) {
        JSONObject o = activeProfile();
        if (o == null) {
            return def;
        }
        String v = o.optString(name, "");
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }

    public String baseUrl() {
        return field("base", DEF_BASE);
    }

    public String model() {
        return field("model", DEF_MODEL);
    }

    public String apiKey() {
        JSONObject o = activeProfile();
        if (o == null) {
            return "";
        }
        String s = o.optString("key", "");
        return s == null ? "" : s.trim();
    }

    public void saveActiveConfig(String base, String key, String model) {
        saveActiveConfig(base, key, model, -1f, 1f, 0);
    }

    public void saveActiveConfig(String base, String key, String model, float temp, int maxTokens) {
        saveActiveConfig(base, key, model, temp, 1f, maxTokens);
    }

    public void saveActiveConfig(String base, String key, String model, float temp, float topP,
                                 int maxTokens) {
        JSONArray a = profilesRaw();
        int i = activeIndex();
        JSONObject o = null;
        if (a.length() > 0 && i < a.length()) {
            o = a.optJSONObject(i);
        }
        if (o == null) {
            o = new JSONObject();
            try {
                o.put("name", "默认");
            } catch (Exception ignored) {
            }
        }
        try {
            o.put("base", base == null ? "" : base.trim());
            o.put("key", key == null ? "" : key.trim());
            o.put("model", model == null ? "" : model.trim());
            o.put("temp", temp);
            o.put("topP", topP);
            o.put("maxTokens", maxTokens);
        } catch (Exception ignored) {
        }
        if (a.length() == 0) {
            a.put(o);
        } else {
            try {
                a.put(i, o);
            } catch (Exception e) {
                a.put(o);
            }
        }
        sp.edit().putString(K_PROFILES, a.toString()).apply();
        if (key != null && !key.trim().isEmpty()) {
            addSavedKey(key.trim());
        }
    }

    /** 取某套配置的字段，越界就回落到当前配置 */
    private String profileField(int index, String name, String def) {
        JSONArray a = profilesRaw();
        int i = (index < 0 || index >= a.length()) ? activeIndex() : index;
        JSONObject o = (i >= 0 && i < a.length()) ? a.optJSONObject(i) : null;
        if (o == null) {
            return def;
        }
        String v = o.optString(name, "");
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }

    public String baseUrlOf(int index) {
        return profileField(index, "base", DEF_BASE);
    }

    public String modelOf(int index) {
        return profileField(index, "model", DEF_MODEL);
    }

    public String apiKeyOf(int index) {
        JSONArray a = profilesRaw();
        int i = (index < 0 || index >= a.length()) ? activeIndex() : index;
        JSONObject o = (i >= 0 && i < a.length()) ? a.optJSONObject(i) : null;
        if (o == null) {
            return "";
        }
        String s = o.optString("key", "");
        return s == null ? "" : s.trim();
    }

    public float tempOf(int index) {
        JSONArray a = profilesRaw();
        int i = (index < 0 || index >= a.length()) ? activeIndex() : index;
        JSONObject o = (i >= 0 && i < a.length()) ? a.optJSONObject(i) : null;
        float t = o == null ? -1f : (float) o.optDouble("temp", -1d);
        return t < 0 ? 0.9f : t;
    }

    public int maxTokensOf(int index) {
        JSONArray a = profilesRaw();
        int i = (index < 0 || index >= a.length()) ? activeIndex() : index;
        JSONObject o = (i >= 0 && i < a.length()) ? a.optJSONObject(i) : null;
        return o == null ? 0 : o.optInt("maxTokens", 0);
    }

    public float topPOf(int index) {
        JSONArray a = profilesRaw();
        int i = (index < 0 || index >= a.length()) ? activeIndex() : index;
        JSONObject o = (i >= 0 && i < a.length()) ? a.optJSONObject(i) : null;
        float p = o == null ? -1f : (float) o.optDouble("topP", -1d);
        return p <= 0f ? 1f : p;
    }

    /** 思考强度：none / low / high / max */
    public String effort() {
        String s = sp.getString("reasoning_effort", "");
        return (s == null || s.trim().isEmpty()) ? "high" : s.trim();
    }

    public void setEffort(String e) {
        sp.edit().putString("reasoning_effort", e == null ? "high" : e.trim()).apply();
    }

    /** 主题：0 跟随系统，1 浅色，2 深色 */
    public int themeMode() {
        return sp.getInt("theme_mode", 0);
    }

    // ---- 资料与头像 ----

    public String userName() {
        String s = sp.getString("user_name", "");
        return (s == null || s.trim().isEmpty()) ? "我" : s.trim();
    }

    public void setUserName(String n) {
        sp.edit().putString("user_name", n == null ? "" : n.trim()).apply();
    }

    public String userDesc() {
        String s = sp.getString("user_desc", "");
        return s == null ? "" : s;
    }

    public void setUserDesc(String d) {
        sp.edit().putString("user_desc", d == null ? "" : d.trim()).apply();
    }

    public String userAvatar() {
        String s = sp.getString("user_avatar", "");
        return s == null ? "" : s;
    }

    public void setUserAvatar(String p) {
        sp.edit().putString("user_avatar", p == null ? "" : p).apply();
    }

    public String aiAvatar() {
        String s = sp.getString("ai_avatar", "");
        return s == null ? "" : s;
    }

    public void setAiAvatar(String p) {
        sp.edit().putString("ai_avatar", p == null ? "" : p).apply();
    }

    public void setThemeMode(int m) {
        sp.edit().putInt("theme_mode", m).apply();
    }

    // ---- 外观：按钮颜色、背景图、磨砂玻璃 ----

    public static final String[] ACCENT_NAMES = {"默认蓝", "青", "绿", "紫", "橙", "玫红"};
    public static final int[] ACCENT_LIGHT = {
            0xFF4F6BFF, 0xFF12A0B4, 0xFF2E9E5B, 0xFF7C5CFF, 0xFFE4801F, 0xFFE0568A};
    public static final int[] ACCENT_DARK = {
            0xFF6B84FF, 0xFF2BB6C8, 0xFF3FBF74, 0xFF9A7DFF, 0xFFF09A44, 0xFFF0729E};

    public int accentPreset() {
        return sp.getInt("accent_preset", 0);
    }

    public void setAccentPreset(int i) {
        sp.edit().putInt("accent_preset", i).apply();
    }

    public String accentCustom() {
        String s = sp.getString("accent_custom", "");
        return s == null ? "" : s;
    }

    public void setAccentCustom(String hex) {
        sp.edit().putString("accent_custom", hex == null ? "" : hex.trim()).apply();
    }

    /** 自定义优先，其次预设。返回 0 表示用主题自带的那支颜色 */
    public int accentColor(boolean dark) {
        String cu = accentCustom();
        if (!cu.isEmpty()) {
            int v = parseColor(cu);
            if (v != 0) {
                return v;
            }
        }
        int i = accentPreset();
        if (i < 0 || i >= ACCENT_LIGHT.length) {
            return 0;
        }
        return dark ? ACCENT_DARK[i] : ACCENT_LIGHT[i];
    }

    /** 认 #RRGGBB 和 #AARRGGBB 两种写法，认不出就返回 0 */
    public static int parseColor(String s) {
        try {
            String t = s == null ? "" : s.trim();
            if (t.startsWith("#")) {
                t = t.substring(1);
            }
            if (t.length() == 6) {
                return 0xFF000000 | (int) Long.parseLong(t, 16);
            }
            if (t.length() == 8) {
                return (int) Long.parseLong(t, 16);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static String hexOf(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    public String bgPath() {
        String s = sp.getString("bg_path", "");
        return s == null ? "" : s;
    }

    public void setBgPath(String p) {
        sp.edit().putString("bg_path", p == null ? "" : p).apply();
    }

    /** 清晰度：30 到 100，数字越大图上留的细节越多 */
    public int bgClarity() {
        int v = sp.getInt("bg_clarity", 70);
        return v < 30 ? 30 : (v > 100 ? 100 : v);
    }

    public void setBgClarity(int v) {
        sp.edit().putInt("bg_clarity", v < 30 ? 30 : (v > 100 ? 100 : v)).apply();
    }

    /** 高斯模糊：0 到 40 */
    public int bgBlur() {
        int v = sp.getInt("bg_blur", 0);
        return v < 0 ? 0 : (v > 40 ? 40 : v);
    }

    public void setBgBlur(int v) {
        sp.edit().putInt("bg_blur", v < 0 ? 0 : (v > 40 ? 40 : v)).apply();
    }

    public boolean frosted() {
        return sp.getBoolean("frosted", false);
    }

    public void setFrosted(boolean on) {
        sp.edit().putBoolean("frosted", on).apply();
    }

    /** 磨砂玻璃的透明度：0 到 80，越大越透 */
    public int frostPercent() {
        int v = sp.getInt("frost_percent", 30);
        return v < 0 ? 0 : (v > 80 ? 80 : v);
    }

    public void setFrostPercent(int v) {
        sp.edit().putInt("frost_percent", v < 0 ? 0 : (v > 80 ? 80 : v)).apply();
    }

    /** 界面动画开关。老机器或者觉得晃眼的可以关掉 */
    public boolean animOn() {
        return sp.getBoolean("anim_on", true);
    }

    public void setAnimOn(boolean on) {
        sp.edit().putBoolean("anim_on", on).apply();
    }

    /** 开屏动画：启动时先亮一下图标再淡进主界面 */
    public boolean splashOn() {
        return sp.getBoolean("splash_on", true);
    }
    public void setSplashOn(boolean on) {
        sp.edit().putBoolean("splash_on", on).apply();
    }

    /** 是否已同意用户协议与隐私协议。只有首次启动会拦 */
    public boolean agreed() {
        return sp.getBoolean("agreed", false);
    }

    public void setAgreed(boolean on) {
        sp.edit().putBoolean("agreed", on).apply();
    }


    // ---- 聊天方式 ----

    /** 气泡分段式：切成多条气泡，一条一条出 */
    public static final int CHAT_BUBBLE = 0;
    /** 普通式：整条回复一个气泡 */
    public static final int CHAT_PLAIN = 1;

    public int chatMode() {
        int v = sp.getInt("chat_mode", CHAT_BUBBLE);
        return (v == CHAT_PLAIN) ? CHAT_PLAIN : CHAT_BUBBLE;
    }

    public void setChatMode(int mode) {
        sp.edit().putInt("chat_mode", mode == CHAT_PLAIN ? CHAT_PLAIN : CHAT_BUBBLE).apply();
    }

    // ---- 工具开关 ----

    public boolean toolEnabled(String id) {
        return sp.getBoolean("tool_on_" + id, true);
    }

    public void setToolEnabled(String id, boolean on) {
        sp.edit().putBoolean("tool_on_" + id, on).apply();
    }

    /** 每几轮自动让元反思层看一眼，这条自动的路也可以关掉 */
    public boolean selfAutoOn() {
        return sp.getBoolean("self_auto_on", true);
    }

    public void setSelfAutoOn(boolean on) {
        sp.edit().putBoolean("self_auto_on", on).apply();
    }

    // ---- 自我设定的准入检查 ----

    /** 设定全文的长度上限，超过就不收，防止把一大段东西灌进系统提示词 */
    public static final int MAX_SELF_CHARS = 6000;

    /**
     * 元反思层给回来的东西像不像一份设定。
     * 它要是把自己的提示词、格式说明原样念回来，这里会拦下来，不让写进设定里。
     */
    public static boolean looksLikeSetting(String body) {
        if (body == null) {
            return false;
        }
        String t = body.trim();
        if (t.length() < 20 || t.length() > MAX_SELF_CHARS) {
            return false;
        }
        String[] bad = {
                "<<<", "元反思层", "默认不动", "只输出这一行", "输出下面两种之一",
                "三条缺一条", "第一人称自述", "逐字保留", "使用者对上一版方案",
                "如果确实不该改", "只输出两个字", "输出格式", "不要输出别的解释",
                "它现在的设定", "最近对话", "请判断它需不需要修改设定",
                "请照这个请求改写设定全文", "你是负责维护", "system 内容",
                "不要用 Markdown 标记", "单独输出一行"
        };
        for (int i = 0; i < bad.length; i++) {
            if (t.contains(bad[i])) {
                return false;
            }
        }
        return true;
    }

    /** 当前是不是深色 */
    public boolean isDark() {
        int m = themeMode();
        if (m == 1) {
            return false;
        }
        if (m == 2) {
            return true;
        }
        try {
            int mode = appCtx.getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        } catch (Exception e) {
            return false;
        }
    }

    // ---- 功能模型：哪件事交给哪套配置去做 ----

    public static final String FN_SELF = "self";
    public static final String FN_COMPRESS = "compress";

    public int functionProfile(String fn) {
        return sp.getInt("fn_profile_" + fn, -1);
    }

    public void setFunctionProfile(String fn, int index) {
        sp.edit().putInt("fn_profile_" + fn, index).apply();
    }

    /** 真正要用的配置下标；没指定就用当前配置 */
    public int pickProfile(String fn) {
        int i = functionProfile(fn);
        if (i < 0 || i >= profileCount()) {
            return activeIndex();
        }
        return i;
    }

    public String fnLabel(String fn) {
        int i = functionProfile(fn);
        if (i < 0 || i >= profileCount()) {
            return "跟当前配置一致（" + activeName() + "）";
        }
        return profileName(i);
    }

    // ---- 功能的自定义提示词 ----

    public String fnPrompt(String fn) {
        String s = sp.getString("fn_prompt_" + fn, "");
        return s == null ? "" : s;
    }

    /** 传空字符串表示改回内置默认 */
    public void setFnPrompt(String fn, String text) {
        sp.edit().putString("fn_prompt_" + fn, text == null ? "" : text).apply();
    }

    public boolean fnPromptCustom(String fn) {
        String s = fnPrompt(fn);
        return s != null && !s.trim().isEmpty();
    }

    /** 实际要用的提示词：没自定义过就回落到内置默认 */
    public String fnPromptOrDefault(String fn) {
        if (fnPromptCustom(fn)) {
            return fnPrompt(fn).trim();
        }
        return FN_SELF.equals(fn) ? DEF_SELF_PROMPT : DEF_COMPRESS_PROMPT;
    }

    public String fnPromptLabel(String fn) {
        if (!fnPromptCustom(fn)) {
            return "提示词：内置默认";
        }
        return "提示词：自定义（" + fnPrompt(fn).trim().length() + " 字）";
    }

    public int createProfile(String name) {
        JSONArray a = profilesRaw();
        JSONObject o = new JSONObject();
        String n = (name == null || name.trim().isEmpty()) ? ("配置 " + (a.length() + 1)) : name.trim();
        try {
            o.put("name", n);
            o.put("base", DEF_BASE);
            o.put("key", "");
            o.put("model", DEF_MODEL);
        } catch (Exception ignored) {
        }
        a.put(o);
        sp.edit().putString(K_PROFILES, a.toString()).putInt(K_ACTIVE, a.length() - 1).apply();
        return a.length() - 1;
    }

    public boolean deleteActiveProfile() {
        JSONArray a = profilesRaw();
        if (a.length() <= 1) {
            return false;
        }
        int i = activeIndex();
        JSONArray b = new JSONArray();
        for (int j = 0; j < a.length(); j++) {
            if (j != i) {
                b.put(a.optJSONObject(j));
            }
        }
        sp.edit().putString(K_PROFILES, b.toString()).putInt(K_ACTIVE, 0).apply();
        return true;
    }

    // ================= key（按配置隔离） =================

    private JSONArray legacyPool() {
        try {
            return new JSONArray(sp.getString(K_KEYPOOL, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void setProfileKeys(int index, JSONArray keys) {
        JSONArray a = profilesRaw();
        if (index < 0 || index >= a.length()) {
            return;
        }
        JSONObject o = a.optJSONObject(index);
        if (o == null) {
            return;
        }
        try {
            o.put("keys", keys);
            a.put(index, o);
        } catch (Exception e) {
            a.put(o);
        }
        sp.edit().putString(K_PROFILES, a.toString()).apply();
    }

    public JSONArray savedKeys() {
        JSONArray a = profilesRaw();
        int i = activeIndex();
        JSONObject o = (a.length() > 0 && i < a.length()) ? a.optJSONObject(i) : null;
        JSONArray keys = (o == null) ? null : o.optJSONArray("keys");
        return keys == null ? new JSONArray() : keys;
    }

    private void migrateKeysIfNeeded() {
        if (sp.getBoolean("key_pool_migrated", false)) {
            return;
        }
        JSONArray a = profilesRaw();
        if (a.length() > 0) {
            JSONObject o = a.optJSONObject(0);
            JSONArray keys = (o == null) ? null : o.optJSONArray("keys");
            if (keys == null || keys.length() == 0) {
                JSONArray legacy = legacyPool();
                if (legacy.length() > 0) {
                    setProfileKeys(0, legacy);
                }
            }
        }
        sp.edit().putBoolean("key_pool_migrated", true).apply();
    }

    public void addSavedKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }
        String k = key.trim();
        JSONArray arr = savedKeys();
        JSONArray out = new JSONArray();
        out.put(k);
        for (int i = 0; i < arr.length(); i++) {
            String s = arr.optString(i, "");
            if (!s.isEmpty() && !s.equals(k)) {
                out.put(s);
            }
        }
        setProfileKeys(activeIndex(), out);
    }

    public void removeSavedKeyAt(int index) {
        JSONArray keys = savedKeys();
        JSONArray out = new JSONArray();
        for (int i = 0; i < keys.length(); i++) {
            String s = keys.optString(i, "");
            if (i != index && !s.isEmpty()) {
                out.put(s);
            }
        }
        setProfileKeys(activeIndex(), out);
    }

    // ================= 核心层（只有使用者能改） =================

    public String coreName() {
        String s = sp.getString(K_CORE_NAME, "");
        return (s == null || s.trim().isEmpty()) ? DEF_NAME : s.trim();
    }

    public String coreStory() {
        String s = sp.getString(K_CORE_STORY, "");
        return s == null ? "" : s;
    }

    public JSONArray coreLog() {
        try {
            return new JSONArray(sp.getString(K_CORE_LOG, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    /** 保存核心设定，并留一条改动摘要 */
    public void saveCore(String name, String story) {
        String oldName = coreName();
        String oldStory = coreStory();
        String nn = (name == null || name.trim().isEmpty()) ? DEF_NAME : name.trim();
        String ns = story == null ? "" : story.trim();
        if (nn.equals(oldName) && ns.equals(oldStory)) {
            return;
        }
        StringBuilder sum = new StringBuilder();
        if (!nn.equals(oldName)) {
            sum.append("名字 ").append(oldName).append(" → ").append(nn);
        }
        if (!ns.equals(oldStory)) {
            if (sum.length() > 0) {
                sum.append("；");
            }
            sum.append("故事 ").append(oldStory.isEmpty() ? "空" : (oldStory.length() + "字"))
               .append(" → ").append(ns.isEmpty() ? "空" : (ns.length() + "字"));
        }
        sp.edit().putString(K_CORE_NAME, nn).putString(K_CORE_STORY, ns).apply();

        JSONArray log = coreLog();
        JSONObject o = new JSONObject();
        try {
            o.put("time", System.currentTimeMillis());
            o.put("summary", sum.toString());
        } catch (Exception ignored) {
        }
        log.put(o);
        sp.edit().putString(K_CORE_LOG, log.toString()).apply();
    }

    // ================= AI 自己的块 =================

    public JSONArray aiBlocks() {
        try {
            return new JSONArray(sp.getString(K_BLOCKS, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveBlocks(JSONArray a) {
        sp.edit().putString(K_BLOCKS, a.toString()).apply();
    }

    public JSONArray aiVersions() {
        try {
            return new JSONArray(sp.getString(K_VERSIONS, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void pushVersion(String reason, String source, JSONArray blocks) {
        JSONArray v = aiVersions();
        JSONObject o = new JSONObject();
        try {
            o.put("time", System.currentTimeMillis());
            o.put("reason", reason);
            o.put("source", source);
            o.put("blocks", blocks);
        } catch (Exception ignored) {
        }
        v.put(o);
        sp.edit().putString(K_VERSIONS, v.toString()).apply();
    }

    private static JSONObject block(String title, String type, String content) {
        JSONObject o = new JSONObject();
        try {
            o.put("title", title);
            o.put("type", type);
            o.put("content", content);
            o.put("time", System.currentTimeMillis());
        } catch (Exception ignored) {
        }
        return o;
    }

    private static JSONArray defaultBlocks() {
        JSONArray a = new JSONArray();
        a.put(block(SELF_TITLE, "text", DEFAULT_SELF));
        return a;
    }

    /** 恢复到出厂设定 */
    public void restoreDefaultSelf() {
        setSelfText(DEFAULT_SELF, "使用者恢复默认设定");
    }

    /** 恢复为通用模板：名字与设定一并换成中性版，也留一条改动痕迹 */
    public void restoreGenericSelf() {
        saveCore(DEF_NAME_GENERIC, "");
        setSelfText(DEFAULT_SELF_GENERIC, "使用者恢复通用模板");
    }

    // ---- 外观与开关 ----

    public String fontPath() {
        String s = sp.getString("font_path", "");
        return s == null ? "" : s;
    }

    public void setFontPath(String p) {
        sp.edit().putString("font_path", p == null ? "" : p).apply();
    }

    /** 思考：让它在回答里先写一段思路 */
    public boolean thinkingOn() {
        return sp.getBoolean("thinking_on", false);
    }

    public void setThinkingOn(boolean on) {
        sp.edit().putBoolean("thinking_on", on).apply();
    }

    /** 实时时间：每轮把当前时间告诉它 */
    public boolean realtimeOn() {
        return sp.getBoolean("realtime_on", false);
    }

    public void setRealtimeOn(boolean on) {
        sp.edit().putBoolean("realtime_on", on).apply();
    }

    private void migrateBlocksIfNeeded() {
        if (sp.getBoolean("blocks_migrated", false)) {
            return;
        }
        if (aiBlocks().length() > 0) {
            sp.edit().putBoolean("blocks_migrated", true).apply();
            return;
        }
        JSONArray old = arr(K_OLD_PROMPTS);
        if (old.length() > 0) {
            JSONArray hist = new JSONArray();
            for (int i = 0; i < old.length(); i++) {
                JSONObject ov = old.optJSONObject(i);
                if (ov == null) {
                    continue;
                }
                JSONArray bs = new JSONArray();
                JSONObject b = new JSONObject();
                try {
                    b.put("title", "我是谁");
                    b.put("type", "text");
                    b.put("content", ov.optString("content", ""));
                    b.put("time", ov.optLong("time", System.currentTimeMillis()));
                } catch (Exception ignored) {
                }
                bs.put(b);
                JSONObject v = new JSONObject();
                try {
                    v.put("time", ov.optLong("time", System.currentTimeMillis()));
                    v.put("reason", ov.optString("reason", ""));
                    v.put("source", "migrate");
                    v.put("blocks", bs);
                } catch (Exception ignored) {
                }
                hist.put(v);
            }
            JSONObject last = hist.optJSONObject(hist.length() - 1);
            JSONArray cur = (last == null) ? null : last.optJSONArray("blocks");
            sp.edit()
              .putString(K_BLOCKS, cur == null ? "[]" : cur.toString())
              .putString(K_VERSIONS, hist.toString())
              .putBoolean("blocks_migrated", true)
              .apply();
        } else {
            JSONArray fresh = defaultBlocks();
            saveBlocks(fresh);
            pushVersion("出厂设定", "system", fresh);
            sp.edit().putBoolean("blocks_migrated", true).apply();
        }
    }

    /** 回滚到某个版本快照（整份写回），并且这次回滚本身也记成一版 */
    public void rollbackBlocksTo(int index) {
        JSONArray v = aiVersions();
        if (index < 0 || index >= v.length()) {
            return;
        }
        JSONObject o = v.optJSONObject(index);
        if (o == null) {
            return;
        }
        JSONArray blocks = o.optJSONArray("blocks");
        JSONArray cur = (blocks == null) ? new JSONArray() : blocks;
        saveBlocks(cur);
        pushVersion("用户回滚到 v" + (index + 1), "rollback", cur);
    }

    public void keepOnlyCurrentAiVersion() {
        JSONArray v = aiVersions();
        if (v.length() <= 1) {
            return;
        }
        JSONArray b = new JSONArray();
        b.put(v.optJSONObject(v.length() - 1));
        sp.edit().putString(K_VERSIONS, b.toString()).apply();
    }

    /** 拼给模型看的块文本 */
    public String segRegex() {
        String s = sp.getString("seg_regex", DEF_SEG_REGEX);
        return (s == null || s.trim().isEmpty()) ? DEF_SEG_REGEX : s.trim();
    }

    public String segCleanup() {
        String s = sp.getString("seg_cleanup", DEF_SEG_CLEANUP);
        return s == null ? "" : s;
    }

    public void saveSegmentation(String regex, String cleanup) {
        sp.edit()
          .putString("seg_regex", regex == null ? "" : regex.trim())
          .putString("seg_cleanup", cleanup == null ? "" : cleanup)
          .apply();
    }

    // ================= 消息 =================

    private JSONArray arr(String key) {
        try {
            return new JSONArray(sp.getString(key, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public JSONArray messages() {
        return arr(K_MSG);
    }

    public void setMessages(JSONArray a) {
        sp.edit().putString(K_MSG, a.toString()).apply();
    }

    public void clearMessages() {
        sp.edit().putString(K_MSG, "[]").apply();
    }

    // ================= 数据导出 / 导入 =================

    public static final String EXPORT_FULL = "full";
    public static final String EXPORT_CONFIG = "config";
    public static final String EXPORT_CHARACTER = "character";

    /** 配置类：接口、key、功能模型选择、外观与界面设置 */
    private static final String[] CONFIG_KEYS = {
            "profiles", "active_profile", "key_pool", "base_url", "api_key", "model",
            "theme_mode", "accent_preset", "accent_custom", "bg_path", "bg_clarity", "bg_blur",
            "frosted", "frost_percent", "anim_on", "splash_on", "font_path", "seg_regex",
            "seg_cleanup", "chat_mode", "self_auto_on", "self_turns", "reasoning_effort",
            "thinking_on", "realtime_on", "compress_on",
            "user_name", "user_desc", "user_avatar", "ai_avatar",
            "model_migrated_v2", "single_block_migrated", "key_pool_migrated", "blocks_migrated"
    };
    private static final String[] CONFIG_PREFIX = {"fn_profile_", "tool_on_"};

    /** 角色类：它的设定与提示词、压缩记忆、前代记录、对话，以及功能模型的提示词 */
    private static final String[] CHAR_KEYS = {
            "core_name", "core_story", "core_log", "ai_blocks", "ai_versions",
            "memories", "message_archive", "messages"
    };
    private static final String[] CHAR_PREFIX = {"fn_prompt_"};

    private static boolean keyMatches(String k, String[] exact, String[] prefix) {
        for (int i = 0; i < exact.length; i++) {
            if (exact[i].equals(k)) {
                return true;
            }
        }
        for (int i = 0; i < prefix.length; i++) {
            if (k.startsWith(prefix[i])) {
                return true;
            }
        }
        return false;
    }

    /** 导出：full 全部，config 配置，character 角色。返回可直接写文件的 JSON */
    public org.json.JSONObject exportData(String type) {
        org.json.JSONObject out = new org.json.JSONObject();
        try {
            out.put("app", "自塑");
            out.put("type", type);
            out.put("version", "0.44");
            out.put("time", System.currentTimeMillis());
            org.json.JSONObject data = new org.json.JSONObject();
            java.util.Map<String, ?> all = sp.getAll();
            for (java.util.Map.Entry<String, ?> e : all.entrySet()) {
                String k = e.getKey();
                boolean take;
                if (EXPORT_FULL.equals(type)) {
                    take = true;
                } else if (EXPORT_CONFIG.equals(type)) {
                    take = keyMatches(k, CONFIG_KEYS, CONFIG_PREFIX);
                } else {
                    take = keyMatches(k, CHAR_KEYS, CHAR_PREFIX);
                }
                if (!take) {
                    continue;
                }
                Object v = e.getValue();
                if (v instanceof java.util.Set) {
                    org.json.JSONArray a = new org.json.JSONArray();
                    for (Object o : (java.util.Set<?>) v) {
                        a.put(String.valueOf(o));
                    }
                    data.put(k, a);
                } else if (v != null) {
                    data.put(k, v);
                }
            }
            out.put("data", data);
        } catch (Exception ignored) {
        }
        return out;
    }

    /** 导入：按文件里的 type 写回，返回写入的键数。full 会先清空再写 */
    public int importData(org.json.JSONObject o) {
        org.json.JSONObject data = o == null ? null : o.optJSONObject("data");
        if (data == null) {
            return 0;
        }
        String type = o.optString("type", EXPORT_FULL);
        android.content.SharedPreferences.Editor ed = sp.edit();
        if (EXPORT_FULL.equals(type)) {
            ed.clear();
        }
        int n = 0;
        java.util.Iterator<String> it = data.keys();
        while (it.hasNext()) {
            String k = it.next();
            // 单类导入只认属于该类的键，防止文件被改过后混进别的东西
            if (EXPORT_CONFIG.equals(type) && !keyMatches(k, CONFIG_KEYS, CONFIG_PREFIX)) {
                continue;
            }
            if (EXPORT_CHARACTER.equals(type) && !keyMatches(k, CHAR_KEYS, CHAR_PREFIX)) {
                continue;
            }
            Object v = data.opt(k);
            if (v == null) {
                continue;
            }
            if (v instanceof Boolean) {
                ed.putBoolean(k, (Boolean) v);
            } else if (v instanceof Integer) {
                ed.putInt(k, (Integer) v);
            } else if (v instanceof Long) {
                ed.putLong(k, (Long) v);
            } else if (v instanceof org.json.JSONArray) {
                org.json.JSONArray a = (org.json.JSONArray) v;
                java.util.Set<String> set = new java.util.HashSet<String>();
                for (int i = 0; i < a.length(); i++) {
                    set.add(a.optString(i, ""));
                }
                ed.putStringSet(k, set);
            } else {
                ed.putString(k, String.valueOf(v));
            }
            n++;
        }
        ed.apply();
        return n;
    }

    // ================= 记忆 =================

    private static final String K_MEM = "memories";
    private static final String K_ARCHIVE = "message_archive";

    public static final int MAX_ACTIVE_MEMORIES = 200;
    public static final int INJECT_MEMORIES = 12;

    public JSONArray memories() {
        return arr(K_MEM);
    }

    private void saveMemories(JSONArray a) {
        sp.edit().putString(K_MEM, a.toString()).apply();
    }

    /** 记忆口令：全局一把，由 AI 保管，首次使用自动生成 */
    private static String genPassword() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    public int activeMemoryCount() {
        JSONArray a = memories();
        int n = 0;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && "active".equals(o.optString("status", "active"))) {
                n++;
            }
        }
        return n;
    }

    /** 记一条新记忆。默认上锁。同内容已存在就跳过，返回 false */
    public boolean addMemory(String kind, String tag, String text, int weight) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            return false;
        }
        if (activeMemoryCount() >= MAX_ACTIVE_MEMORIES) {
            return false;
        }
        String norm = normalizeForDup(t);
        JSONArray a = memories();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null || !"active".equals(o.optString("status", "active"))) {
                continue;
            }
            if (norm.equals(normalizeForDup(o.optString("text", "")))) {
                return false;
            }
        }
        JSONObject o = new JSONObject();
        try {
            o.put("id", String.valueOf(System.currentTimeMillis()) + "_" + a.length());
            o.put("kind", normKind(kind));
            o.put("tag", tag == null ? "" : tag.trim());
            o.put("text", t);
            o.put("weight", Math.max(1, Math.min(5, weight)));
            o.put("created", System.currentTimeMillis());
            o.put("lastUsed", System.currentTimeMillis());
            o.put("status", "active");
            o.put("locked", true);
            // 记下来源：写下这条时对话已经有多少条消息，深度删除要用它回退
            o.put("src", messages().length());
        } catch (Exception ignored) {
        }
        a.put(o);
        saveMemories(a);
        return true;
    }

    /** 同一个主题标签的旧条目标记为已被取代，再写新的一条 */
    private static String normalizeForDup(String s) {
        if (s == null) {
            return "";
        }
        return s.toLowerCase().replaceAll("[\\s，。！？、,.!?~～:：;；\"'“”‘’()（）\\[\\]【】]", "");
    }

    private static String normKind(String kind) {
        String k = kind == null ? "" : kind.trim();
        if ("preference".equals(k) || "event".equals(k) || "plan".equals(k) || "log".equals(k)) {
            return k;
        }
        return "fact";
    }

    public static String kindName(String kind) {
        if ("preference".equals(kind)) {
            return "偏好";
        }
        if ("event".equals(kind)) {
            return "事件";
        }
        if ("plan".equals(kind)) {
            return "约定";
        }
        if ("log".equals(kind)) {
            return "日志";
        }
        return "事实";
    }

    // ---- 上下文压缩 ----

    public boolean compressionOn() {
        return sp.getBoolean("compress_on", true);
    }

    public void setCompressionOn(boolean on) {
        sp.edit().putBoolean("compress_on", on).apply();
    }

    /** 把最老的一段对话挪进归档并从活动上下文里去掉，记忆成了唯一的长期载体 */
    public void archiveCompressed(int cut) {
        JSONArray cur = messages();
        if (cut <= 0 || cur.length() <= cut) {
            return;
        }
        JSONArray head = new JSONArray();
        for (int i = 0; i < cut; i++) {
            head.put(cur.optJSONObject(i));
        }
        JSONArray rest = new JSONArray();
        for (int i = cut; i < cur.length(); i++) {
            rest.put(cur.optJSONObject(i));
        }
        JSONArray a = archives();
        JSONObject o = new JSONObject();
        try {
            o.put("time", System.currentTimeMillis());
            o.put("kind", "compress");
            o.put("messages", head);
        } catch (Exception ignored) {
        }
        a.put(o);
        sp.edit().putString(K_ARCHIVE, a.toString()).putString(K_MSG, rest.toString()).apply();

        // 位置整体前移，记忆的来源和删除检查点都要跟着挪
        JSONArray mem = memories();
        boolean changed = false;
        for (int i = 0; i < mem.length(); i++) {
            JSONObject m = mem.optJSONObject(i);
            if (m == null) {
                continue;
            }
            int src = m.optInt("src", 0);
            if (src > 0) {
                int ns = Math.max(0, src - cut);
                if (ns != src) {
                    try {
                        m.put("src", ns);
                        mem.put(i, m);
                        changed = true;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if (changed) {
            saveMemories(mem);
        }
    }

    /** 拼给模型看的记忆段，同时刷新命中时间 */
    public String memoriesForPrompt() {
        JSONArray a = memories();
        ListLike list = new ListLike();
        long now = System.currentTimeMillis();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null || !"active".equals(o.optString("status", "active"))) {
                continue;
            }
            list.add(o);
        }
        list.sort();
        if (list.items.isEmpty()) {
            return "（还没有任何记忆）";
        }
        StringBuilder sb = new StringBuilder();
        int n = Math.min(INJECT_MEMORIES, list.items.size());
        for (int i = 0; i < n; i++) {
            JSONObject o = list.items.get(i);
            String tag = o.optString("tag", "");
            sb.append("- [").append(kindName(o.optString("kind", "fact"))).append("]");
            if (!tag.isEmpty()) {
                sb.append(" ").append(tag).append("：");
            } else {
                sb.append(" ");
            }
            sb.append(o.optString("text", "")).append("\n");
            try {
                o.put("lastUsed", now);
            } catch (Exception ignored) {
            }
        }
        saveMemories(a);
        return sb.toString().trim();
    }

    private static class ListLike {
        final List<JSONObject> items = new ArrayList<JSONObject>();

        void add(JSONObject o) {
            items.add(o);
        }

        void sort() {
            Collections.sort(items, new Comparator<JSONObject>() {
                public int compare(JSONObject x, JSONObject y) {
                    int wx = x.optInt("weight", 3);
                    int wy = y.optInt("weight", 3);
                    if (wx != wy) {
                        return wy - wx;
                    }
                    return Long.compare(y.optLong("lastUsed", 0), x.optLong("lastUsed", 0));
                }
            });
        }
    }

    // ---- 待删除（两道门的第一道之后） ----
    public int deleteMemoriesDirect(JSONArray ids) {
        if (ids == null || ids.length() == 0) {
            return 0;
        }
        JSONArray a = memories();
        JSONArray keep = new JSONArray();
        int removed = 0;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) {
                continue;
            }
            String id = o.optString("id", "");
            boolean hit = false;
            for (int j = 0; j < ids.length(); j++) {
                if (id.equals(ids.optString(j, ""))) {
                    hit = true;
                }
            }
            if (hit) {
                removed++;
            } else {
                keep.put(o);
            }
        }
        saveMemories(keep);
        return removed;
    }

    /** 整库销毁：不需要口令，不可恢复 */
    public void destroyMemories() {
        sp.edit().putString(K_MEM, "[]").apply();
    }

    /** 完全重置：清记忆 + 块回出厂（核心层保留） */
    public void fullReset() {
        JSONArray fresh = defaultBlocks();
        saveBlocks(fresh);
        pushVersion("完全重置", "system", fresh);
        destroyMemories();
    }

    // ---- 前代记录 ----

    public JSONArray archives() {
        return arr(K_ARCHIVE);
    }

    /** 把当前对话挪进前代记录，AI 之后读不到 */
    public void archiveMessages() {
        JSONArray cur = messages();
        if (cur.length() == 0) {
            clearMessages();
            return;
        }
        JSONArray a = archives();
        JSONObject o = new JSONObject();
        try {
            o.put("time", System.currentTimeMillis());
            o.put("messages", cur);
        } catch (Exception ignored) {
        }
        a.put(o);
        sp.edit().putString(K_ARCHIVE, a.toString()).putString(K_MSG, "[]").apply();
    }

    public void deleteArchiveAt(int index) {
        JSONArray a = archives();
        JSONArray b = new JSONArray();
        for (int i = 0; i < a.length(); i++) {
            if (i != index) {
                b.put(a.optJSONObject(i));
            }
        }
        sp.edit().putString(K_ARCHIVE, b.toString()).apply();
    }
}
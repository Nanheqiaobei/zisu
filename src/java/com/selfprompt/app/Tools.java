// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AI 能用的工具都登记在这里。侧边栏「工具」页读的就是这张表，
 * 发给接口的 tools 数组也由这里按开关筛出来。
 */
public class Tools {

    public static final String SELF_CHANGE = "request_self_change";
    public static final String WRITE_DIARY = "write_diary";

    public static class Info {
        public String id;
        public String name;
        public String desc;
        public String note;

        Info(String id, String name, String desc, String note) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.note = note;
        }
    }

    public static Info[] all() {
        return new Info[]{
                new Info(SELF_CHANGE,
                        "修改自己的设定",
                        "向系统提一个改自己设定的请求。它只提请求，不动笔；真正落笔的是它自己的元反思层"
                                + "方案会先弹出来问你，你点头才写进版本历史，随时能回滚。",
                        "关掉之后，这条对话里它就不能主动提修改了。系统每几轮自动看一眼的那条路"
                                + "不受影响，只是它自己开不了口"),
                new Info(WRITE_DIARY,
                        "写日记",
                        "它把当天值得记的事写成一篇日记，存进「脑」的日记目录，按日期命名。"
                                + "写入由它自己完成，不需要你确认",
                        "这个工具需要有文件访问权限，并且「脑」的位置已设好。日记只增不删，"
                                + "同一天再写会追加到当天那篇后面")
        };
    }

    public static Info find(String id) {
        Info[] a = all();
        for (int i = 0; i < a.length; i++) {
            if (a[i].id.equals(id)) {
                return a[i];
            }
        }
        return null;
    }

    /** 拼给接口的 tools 数组，只放开了开关的 */
    public static JSONArray buildEnabled(Store s) {
        JSONArray arr = new JSONArray();
        try {
            Info[] a = all();
            for (int i = 0; i < a.length; i++) {
                if (s != null && !s.toolEnabled(a[i].id)) {
                    continue;
                }
                if (SELF_CHANGE.equals(a[i].id)) {
                    arr.put(selfChangeSchema());
                } else if (WRITE_DIARY.equals(a[i].id)) {
                    arr.put(writeDiarySchema());
                }
            }
        } catch (Exception ignored) {
        }
        return arr;
    }

    private static JSONObject selfChangeSchema() throws Exception {
        JSONObject reason = new JSONObject().put("type", "string")
                .put("description", "你为什么想改自己的设定，要求说清楚");
        JSONObject hint = new JSONObject().put("type", "string")
                .put("description", "你希望怎么改，比如要加什么、要去掉什么、想变成什么样。可以留空");
        JSONObject props = new JSONObject().put("reason", reason).put("hint", hint);
        JSONObject params = new JSONObject()
                .put("type", "object")
                .put("properties", props)
                .put("required", new JSONArray().put("reason"));
        JSONObject fn = new JSONObject()
                .put("name", SELF_CHANGE)
                .put("description", "向系统提出修改你自己设定的请求。你只提请求，不改写；"
                        + "落笔是它自己的元反思层做的，方案会先弹出来由使用者确认，确认后才写进版本历史。"
                        + "提了之后本轮就到这儿了，不会有结果再回到你手里，所以同一件事不要连着提第二次。")
                .put("parameters", params);
        return new JSONObject().put("type", "function").put("function", fn);
    }

    /** 写日记：把当天的事写成一篇，存进「脑」的日记目录 */
    private static JSONObject writeDiarySchema() throws Exception {
        JSONObject content = new JSONObject().put("type", "string")
                .put("description", "日记正文。用第一人称写，写当天真正发生的事与你的想法，"
                        + "不要客套、不要复述对话。可以分段。"
                        + "同一天内已经写过的事不要重复写；写之前先想清楚今天真正的新内容是什么");
        JSONObject props = new JSONObject().put("content", content);
        JSONObject params = new JSONObject()
                .put("type", "object")
                .put("properties", props)
                .put("required", new JSONArray().put("content"));
        JSONObject fn = new JSONObject()
                .put("name", WRITE_DIARY)
                .put("description", "把今天值得记的事写成一篇日记，存进「脑」的日记目录，按日期命名。"
                        + "同一天再写会追加到当天那篇后面；同一天内重复的内容系统会拒收，"
                        + "所以写之前先想清楚这次真正要记的新事。写完本轮就到这儿了，不会有结果回到你手里。")
                .put("parameters", params);
        return new JSONObject().put("type", "function").put("function", fn);
    }
}
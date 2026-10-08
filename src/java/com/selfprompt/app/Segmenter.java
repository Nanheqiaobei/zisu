// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把一条 AI 回复切成多条聊天气泡。
 * 规则分两层：先按换行切（聊天软件的直觉），某一段太长时再按句末标点切。
 * 句末标点正则与内容过滤正则可在设置页自定义，默认值参照 AstrBot 的 segmented_reply。
 */
public class Segmenter {

    /** 单段超过这个字数，才按句末标点再切 */
    public static final int SOFT_LIMIT = 60;
    /** 单段硬上限，超过就机械切断（防止一句话没有标点导致气泡过长） */
    public static final int HARD_LIMIT = 160;
    /** 整条回复超过这个字数就不再分段，直接一整块，避免刷屏 */
    public static final int NO_SPLIT_TOTAL = 800;

    public static List<String> split(String text, String regex, String cleanup) {
        return split(text, regex, cleanup, SOFT_LIMIT, HARD_LIMIT, NO_SPLIT_TOTAL);
    }

    public static List<String> split(String text, String regex, String cleanup,
                                     int softLimit, int hardLimit, int noSplitTotal) {
        List<String> out = new ArrayList<String>();
        if (text == null) {
            return out;
        }
        Pattern sentence = compile(regex);
        Pattern clean = compile(cleanup);

        String t = text.replace("\r\n", "\n").replace("\r", "\n").trim();
        if (t.isEmpty()) {
            return out;
        }
        if (t.length() > noSplitTotal) {
            addIfNotEmpty(out, clean(t, clean));
            return out;
        }

        String[] lines = t.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.length() <= softLimit) {
                addIfNotEmpty(out, clean(line, clean));
            } else {
                List<String> parts = splitBySentence(line, sentence, softLimit, hardLimit);
                for (int j = 0; j < parts.size(); j++) {
                    addIfNotEmpty(out, clean(parts.get(j), clean));
                }
            }
        }
        return out;
    }

    /** 正则无效时回退到默认值，不让用户写错就把功能弄坏 */
    private static Pattern compile(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            return null;
        }
        try {
            return Pattern.compile(regex.trim(), Pattern.DOTALL);
        } catch (Exception e) {
            try {
                return Pattern.compile(Store.DEF_SEG_REGEX, Pattern.DOTALL);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private static List<String> splitBySentence(String line, Pattern sentence,
                                                int softLimit, int hardLimit) {
        List<String> parts = new ArrayList<String>();
        if (sentence == null) {
            parts.add(line);
        } else {
            Matcher m = sentence.matcher(line);
            StringBuilder buf = new StringBuilder();
            while (m.find()) {
                String s = m.group().trim();
                if (s.isEmpty()) {
                    continue;
                }
                if (buf.length() > 0 && buf.length() + s.length() > softLimit) {
                    parts.add(buf.toString());
                    buf.setLength(0);
                }
                buf.append(s);
            }
            if (buf.length() > 0) {
                parts.add(buf.toString());
            }
        }
        if (parts.isEmpty()) {
            parts.add(line);
        }

        List<String> fixed = new ArrayList<String>();
        for (int i = 0; i < parts.size(); i++) {
            String p = parts.get(i);
            if (p.length() <= hardLimit) {
                fixed.add(p);
                continue;
            }
            int start = 0;
            while (start < p.length()) {
                int end = Math.min(p.length(), start + hardLimit);
                fixed.add(p.substring(start, end));
                start = end;
            }
        }
        return fixed;
    }

    private static void addIfNotEmpty(List<String> out, String s) {
        if (s != null && !s.trim().isEmpty()) {
            out.add(s.trim());
        }
    }

    private static String clean(String s, Pattern clean) {
        if (s == null) {
            return "";
        }
        if (clean == null) {
            return s;
        }
        try {
            return clean.matcher(s).replaceAll("");
        } catch (Exception e) {
            return s;
        }
    }
}
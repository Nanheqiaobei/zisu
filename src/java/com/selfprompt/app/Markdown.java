// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

/** 轻量 Markdown 渲染：只处理聊天里真的会出现的几种标记，不引第三方库 */
public class Markdown {

    public static CharSequence render(String src) {
        if (src == null || src.isEmpty()) {
            return "";
        }
        String text = src.replace("\r\n", "\n");
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String t = line;
            String trimmed = t.trim();

            if (trimmed.startsWith("```")) {
                // 代码围栏行本身不显示
                continue;
            }
            if (trimmed.startsWith("#")) {
                t = trimmed.replaceFirst("^#+\\s*", "");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
                t = "· " + trimmed.substring(2).trim();
            } else if (trimmed.startsWith(">")) {
                t = "　" + trimmed.substring(1).trim();
            } else if (trimmed.matches("-{3,}|_{3,}|\\*{3,}")) {
                t = "———————";
            }

            appendInline(sb, t);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb;
    }

    private static void appendInline(SpannableStringBuilder sb, String line) {
        int i = 0;
        int n = line.length();
        while (i < n) {
            if (line.startsWith("**", i)) {
                int e = line.indexOf("**", i + 2);
                if (e > i + 2) {
                    int s = sb.length();
                    sb.append(line, i + 2, e);
                    sb.setSpan(new StyleSpan(Typeface.BOLD), s, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = e + 2;
                    continue;
                }
            }
            char c = line.charAt(i);
            if (c == '`') {
                int e = line.indexOf('`', i + 1);
                if (e > i + 1) {
                    int s = sb.length();
                    sb.append(line, i + 1, e);
                    sb.setSpan(new TypefaceSpan("monospace"), s, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new BackgroundColorSpan(0x14000000), s, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = e + 1;
                    continue;
                }
            }
            if (line.startsWith("~~", i)) {
                int e = line.indexOf("~~", i + 2);
                if (e > i + 2) {
                    int s = sb.length();
                    sb.append(line, i + 2, e);
                    sb.setSpan(new StrikethroughSpan(), s, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = e + 2;
                    continue;
                }
            }
            if (c == '*') {
                int e = line.indexOf('*', i + 1);
                if (e > i + 1) {
                    int s = sb.length();
                    sb.append(line, i + 1, e);
                    sb.setSpan(new StyleSpan(Typeface.ITALIC), s, sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = e + 1;
                    continue;
                }
                // 落单的星号直接丢掉，免得满屏符号
                i++;
                continue;
            }
            if (c == '[') {
                int close = line.indexOf(']', i);
                if (close > i && close + 1 < n && line.charAt(close + 1) == '(') {
                    int end = line.indexOf(')', close);
                    if (end > close) {
                        sb.append(line, i + 1, close);
                        i = end + 1;
                        continue;
                    }
                }
            }
            sb.append(c);
            i++;
        }
    }
}
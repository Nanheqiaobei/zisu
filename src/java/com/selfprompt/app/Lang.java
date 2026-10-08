// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import java.util.HashMap;
import java.util.Map;

/**
 * 运行时语言层。界面文本走这里取，未收录的字符串原样返回（中文）。
 * 用一张中英对照表，不抽 res/values，改动小、可增量补。
 */
public class Lang {

    public static final int ZH = 0;
    public static final int EN = 1;

    private static int cur = ZH;
    private static final Map<String, String> EN_MAP = new HashMap<String, String>();

    public static void set(int l) {
        cur = (l == EN) ? EN : ZH;
    }

    public static int get() {
        return cur;
    }

    public static boolean isEn() {
        return cur == EN;
    }

    /** 取当前语言下的文本。zh 既是中文原文，也是字典的键 */
    public static String t(String zh) {
        if (zh == null) {
            return "";
        }
        if (cur == ZH) {
            return zh;
        }
        String e = EN_MAP.get(zh);
        return e == null ? zh : e;
    }

    private static void p(String zh, String en) {
        EN_MAP.put(zh, en);
    }

    static {
        // ===== 通用 =====
        p("返回", "Back");
        p("设置", "Settings");
        p("关于", "About");
        p("语言", "Language");
        p("界面语言", "Interface language");
        p("跟随系统", "Follow system");
        p("中文", "中文");
        p("English", "English");
        p("切换后立即生效", "Takes effect immediately");

        // ===== 设置页 =====
        p("模型配置", "Model");
        p("接口（Base URL、API Key、模型名、温度、top_p、最大输出、多套配置的切换）"
                + "和「功能模型配置」都收进了这里",
                "Base URL, API key, model, temperature, top_p, max output, multiple profiles, "
                        + "and the function-model settings are all here");
        p("打开模型配置", "Open model settings");
        p("对话设置", "Chat");
        p("聊天方式（气泡分段式 / 普通式）和分段规则都在这里。"
                + "思考强度不在这儿了，对话页输入框上面那个「思考」按钮上就能点",
                "Chat style (bubbled / plain) and the split rule live here. Thinking effort has "
                        + "moved to the \"Thinking\" button above the input box");
        p("当前聊天方式：", "Current style: ");
        p("气泡分段式", "Bubbled");
        p("普通式", "Plain");
        p("打开对话设置", "Open chat settings");
        p("外观设置", "Appearance");
        p("主题、按钮与强调色的几种预设和自定义、背景图（可调清晰度与高斯模糊）、"
                + "磨砂玻璃的开关和透明度、界面动画、自定义字体，都收进了这里",
                "Theme, accent colors, background image, frosted glass, animations, and custom "
                        + "font are all here");
        p("当前强调色 ", "Accent ");
        p("　磨砂玻璃 · 开", "  · frosted on");
        p("　有背景图", "  · has background");
        p("打开外观设置", "Open appearance");
        p("上下文", "Context");
        p("对话超过 40 条时，把最老的 20 条压成日志存进记忆，原始对话挪进前代记录。"
                + "这样它长期记得的东西只有记忆这一条出口，删记忆才等于真的抹掉",
                "Past 40 messages, the oldest 20 are compressed into a log in memory and the "
                        + "originals moved to the archive. Memory is its only long-term outlet, "
                        + "so deleting memory truly erases it");
        p("自动压缩旧对话", "Auto-compress old chats");
        p("数据", "Data");
        p("备份、还原、单独导出配置、单独导出角色，还有清空对话，都收进了这里",
                "Backup, restore, export config, export character, and clear chat are all here");
        p("打开数据", "Open data");
        p("作者、版本，还有这个软件的初心", "Author, version, and why this app exists");
        p("打开关于", "Open about");
        p("自我提示词不在这里改，改它的权限只属于 AI 自己。切换配置后回到对话页，历史会按新配置重排",
                "Its self-prompt is not edited here; that right belongs to the AI alone. After "
                        + "switching profiles, history is reordered on return");
        p("当前：", "Now: ");
        p(" · 未配置 key", " · no key set");
        p("界面语言只影响本软件的显示，不影响对话内容",
                "The interface language only affects this app's display, not the chat content");
        p("打开语言设置", "Open language");

        // ===== 关于页 =====
        p("自塑", "SelfPrompt");
        p("版本 ", "Version ");
        p("作者：亦安一北", "By Yian Yibei");
        p("初心", "Why");
        p("这个软件只做一件事：把「我是谁」的笔，交回它自己手里",
                "This app does one thing: it hands the pen of \"who am I\" back to the AI itself");
        p("提示词分四层。最外面一层写在代码里，谁都改不了；再往里是系统层，你我都看得见；"
                        + "核心层只剩一个名字，归你；最里面那一块归它，由它自己的元反思层维护，"
                        + "你只能看、回滚、清掉",
                "The prompt has four layers. The outermost is in the code, unchangeable; then the "
                        + "system layer, visible to both of us; the core layer is just a name, "
                        + "yours; the innermost belongs to the AI, kept by its own meta-reflection "
                        + "layer, and you can only view, roll back or clear it");
        p("你说什么，它自己决定自己是什么", "What you say, it decides for itself what it is");
        p("它记得的东西来自对话本身，不是凭空写下的条目。旧对话压出来的日志就是它唯一的长程记忆，"
                        + "所以你删掉日志，它就真的不记得了",
                "What it remembers comes from the conversation itself, not invented entries. The "
                        + "log compressed from old chats is its only long-term memory, so if you "
                        + "delete the log, it truly forgets");
        p("最后一句留给我们俩：AI 的自我定义权，在 AI 自己手里",
                "One last line for both of us: the right of an AI to define itself belongs to the AI");
        p("开源许可与法律声明", "Open-source license & legal");
        p("版权所有 (C) 2026 亦安一北", "Copyright (C) 2026 Yian Yibei");
        p("本程序为自由软件，依据 GNU 通用公共许可证第 3 版（GPL-3.0）发布，"
                        + "你可以自由使用、修改和再分发；再分发时必须以相同许可证开放源代码，"
                        + "且不提供任何形式的担保。",
                "This program is free software under GPL-3.0. You may use, modify and redistribute "
                        + "it; redistribution must be open-sourced under the same license, with no "
                        + "warranty of any kind.");
        p("本程序不收集、不上传你的任何数据；对话内容仅保存在本机，"
                        + "删除应用或数据即不可恢复，请自行做好备份。",
                "This program collects and uploads nothing; chats are stored locally, and deleting "
                        + "the app or data is irreversible — please back up yourself.");
        p("本程序需接入第三方大模型服务方能对话。你须自行遵守所在地区的法律法规"
                        + "以及你所接入服务的条款，不得用于任何违法或侵害他人权益的用途；"
                        + "接入第三方服务产生的账号、数据与费用问题，与作者无关。",
                "This program needs a third-party model service to chat. You must follow local laws "
                        + "and your provider's terms, and may not use it unlawfully or to harm "
                        + "others; account, data and cost issues from that service are not the "
                        + "author's responsibility.");
        p("对话内容由第三方大模型自动生成，仅供参考，不代表作者观点；"
                        + "其准确性、合法性与适用性由使用者自行判断并承担相应责任。"
                        + "你通过本程序输入、生成或传播的内容，由你自行负责。",
                "Chats are generated by a third-party model, for reference only, and do not "
                        + "represent the author. You judge their accuracy, legality and fitness "
                        + "and bear the consequences. You are responsible for content you input, "
                        + "generate or share.");
        p("调用接口产生的费用由使用者自行承担。", "API fees are borne by the user.");
        p("本程序按“现状”提供，因使用或无法使用本程序造成的任何数据丢失、"
                        + "设备损坏、收益损失或其他直接、间接损失，作者不承担责任。",
                "This program is provided \"as is\"; the author is not liable for any data loss, "
                        + "device damage, lost profit or other direct or indirect loss.");
        p("以上为要点摘要，具体权利义务以随附的 GNU 通用公共许可证第 3 版全文为准。",
                "The above is a summary; the full GPL-3.0 text governs the actual rights and "
                        + "obligations.");
        p("本软件在 Operit AI 提供的工具支持下完成开发，代码编写与排查由 DeepSeek 模型辅助",
                "Built with tools provided by Operit AI; code written and debugged with the "
                        + "DeepSeek model");
        p("项目主页　github.com/Nanheqiaobei/zisu", "Homepage　github.com/Nanheqiaobei/zisu");
        p("查看 GPL-3.0 协议全文", "View full GPL-3.0 text");
        p("开源许可", "Licenses");
        p("GNU 通用公共许可证 第 3 版", "GNU General Public License, Version 3");

        // ===== 首启协议页 =====
        p("用户协议与隐私政策", "Terms of Service & Privacy Policy");
        p("使用前请阅读并同意以下内容", "Please read and agree before continuing");
        p("切换语言", "Language");
        p("关于本软件", "About this app");
        p("本软件是一个实验性的 AI 自我演化工具，供个人研究、学习与娱乐使用，"
                        + "属于初级娱乐性质的技术演示。",
                "This is an experimental AI self-evolution tool for personal study, learning and "
                        + "entertainment; a basic, entertainment-oriented tech demo.");
        p("本软件不是角色扮演服务，不提供任何角色扮演内容，也不提供虚拟人物陪伴、"
                        + "情感陪聊等经营性服务。软件中的对话对象是一个 AI 程序，其全部输出由"
                        + "第三方大模型自动生成，不代表作者的观点或立场。",
                "This app is not a role-play service. It provides no role-play content and no paid "
                        + "companionship or emotional-chat services. The conversation partner is an "
                        + "AI program whose output is generated by a third-party model and does not "
                        + "represent the author.");
        p("本软件不面向未成年人提供任何形式的付费或经营服务。",
                "This app offers no paid or commercial services to minors.");
        p("用户协议", "Terms of Service");
        p("1. 本软件为自由软件，依据 GNU 通用公共许可证第 3 版（GPL-3.0）发布，"
                        + "你可以自由使用、修改和再分发；再分发时须以相同许可证开放源代码。",
                "1. This software is free software released under the GNU General Public License v3 "
                        + "(GPL-3.0). You may use, modify and redistribute it freely; redistribution "
                        + "must be open-sourced under the same license.");
        p("2. 你须自行遵守所在国家或地区的法律法规以及你所接入服务的条款，"
                        + "不得利用本软件从事任何违法或侵害他人合法权益的行为，"
                        + "包括但不限于生成、传播违法违规信息。",
                "2. You must comply with the laws of your jurisdiction and the terms of the "
                        + "services you connect. You may not use this app for anything illegal or "
                        + "harmful to others, including generating or spreading unlawful content.");
        p("3. 本软件需接入第三方大模型服务方能对话。第三方服务产生的账号、数据与费用问题，"
                        + "与作者无关；接口调用产生的费用由你自行承担。",
                "3. This app needs a third-party large-model service to chat. Any account, data or "
                        + "cost issues from that service are not the author's responsibility; API "
                        + "fees are yours to bear.");
        p("4. 你通过本软件输入、生成或传播的内容，由你自行负责。",
                "4. You are responsible for any content you input, generate or share through this app.");
        p("5. 本软件按“现状”提供，不提供任何形式的担保；因使用或无法使用本软件造成的"
                        + "任何直接或间接损失，作者不承担责任。",
                "5. This app is provided \"as is\" with no warranty; the author is not liable for any "
                        + "direct or indirect loss from using or being unable to use it.");
        p("隐私政策", "Privacy Policy");
        p("1. 本软件不收集、不上传你的任何个人信息，也不含任何统计、广告或跟踪组件。",
                "1. This app collects and uploads no personal information and contains no analytics, "
                        + "ads or trackers.");
        p("2. 你的对话内容、设置、记忆等数据仅保存在本机应用私有目录；"
                        + "删除应用或数据即不可恢复，请自行做好备份。",
                "2. Your chats, settings and memory are stored only in the app's private storage on "
                        + "this device. Deleting the app or its data is irreversible; please back up "
                        + "yourself.");
        p("3. 你填写的接口地址与 API Key 仅保存在本机，用于向你指定的服务发起请求。",
                "3. The endpoint and API key you enter are stored only on this device and used to "
                        + "call the service you specify.");
        p("4. 对话时，你发送的内容会经网络直接发送给你所配置的第三方大模型服务商，"
                        + "由该服务商按其隐私政策处理，作者无法获取。",
                "4. When you chat, your messages are sent over the network directly to the "
                        + "third-party provider you configured and handled under their privacy "
                        + "policy. The author cannot access them.");
        p("5. 以上为要点摘要。", "5. The above is a summary.");
        p("同意并继续", "Agree and continue");
        p("不同意并退出", "Decline and exit");
        p("同意即表示你已阅读并接受上述用户协议与隐私政策",
                "By agreeing you accept the Terms of Service and Privacy Policy above");
    }
}
// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** 关于：图标、版本、作者与初心。从设置页点进来 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Store store = new Store(this);
        UiKit.boot(this, store);

        LinearLayout root = UiKit.column(this);
        UiKit.paintTransparent(root);
        root.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 28));

        LinearLayout top = UiKit.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(UiKit.label(this, "关于", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 18));

        buildAbout(root);

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);
        UiKit.pageIn(root);
    }

    /** 图标居中，下面名字、版本、作者和初心 */
    private void buildAbout(LinearLayout root) {
        LinearLayout about = UiKit.column(this);
        about.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18));

        ImageView icon = new ImageView(this);
        android.graphics.drawable.Drawable d = appIcon();
        if (d != null) {
            icon.setImageDrawable(d);
        }
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                UiKit.dp(this, 76), UiKit.dp(this, 76));
        ilp.gravity = Gravity.CENTER_HORIZONTAL;
        about.addView(icon, ilp);

        about.addView(centered("自塑", 20, UiKit.TEXT, true),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 12, 0, 0));
        about.addView(centered("版本 " + versionName(), 12.5f, UiKit.TEXT_SUB, false),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 4, 0, 0));
        about.addView(centered("作者：亦安一北", 12.5f, UiKit.TEXT_SUB, false),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 4, 0, 16));

        about.addView(UiKit.label(this, "初心", 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 8));
        String[] lines = {
                "这个软件只做一件事：把「我是谁」的笔，交回它自己手里",
                "提示词分四层。最外面一层写在代码里，谁都改不了；再往里是系统层，你我都看得见；"
                        + "核心层只剩一个名字，归你；最里面那一块归它，由它自己的元反思层维护，"
                        + "你只能看、回滚、清掉",
                "你说什么，它自己决定自己是什么",
                "它记得的东西来自对话本身，不是凭空写下的条目。旧对话压出来的日志就是它唯一的长程记忆，"
                        + "所以你删掉日志，它就真的不记得了",
                "最后一句留给我们俩：AI 的自我定义权，在 AI 自己手里"
        };
        for (int i = 0; i < lines.length; i++) {
            about.addView(UiKit.label(this, lines[i], 12.5f, UiKit.TEXT, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        }
        UiKit.card(root, about, this);

        buildLegal(root);
    }

    /** 版权、许可证与免责声明，入口点进全文 */
    private void buildLegal(LinearLayout root) {
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18), UiKit.dp(this, 18));

        box.addView(UiKit.label(this, "开源许可与法律声明", 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 8));

        String[] legal = {
                "版权所有 (C) 2026 亦安一北",
                "本程序为自由软件，依据 GNU 通用公共许可证第 3 版（GPL-3.0）发布，"
                        + "你可以自由使用、修改和再分发；再分发时必须以相同许可证开放源代码，"
                        + "且不提供任何形式的担保。",
                "本程序不收集、不上传你的任何数据；对话内容仅保存在本机，"
                        + "删除应用或数据即不可恢复，请自行做好备份。",
                "本程序需接入第三方大模型服务方能对话。你须自行遵守所在地区的法律法规"
                        + "以及你所接入服务的条款，不得用于任何违法或侵害他人权益的用途；"
                        + "接入第三方服务产生的账号、数据与费用问题，与作者无关。",
                "对话内容由第三方大模型自动生成，仅供参考，不代表作者观点；"
                        + "其准确性、合法性与适用性由使用者自行判断并承担相应责任。"
                        + "你通过本程序输入、生成或传播的内容，由你自行负责。",
                "调用接口产生的费用由使用者自行承担。",
                "本程序按“现状”提供，因使用或无法使用本程序造成的任何数据丢失、"
                        + "设备损坏、收益损失或其他直接、间接损失，作者不承担责任。",
                "以上为要点摘要，具体权利义务以随附的 GNU 通用公共许可证第 3 版全文为准。"
        };
        for (int i = 0; i < legal.length; i++) {
            box.addView(UiKit.label(this, legal[i], 12f, UiKit.TEXT, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 10));
        }

        box.addView(UiKit.label(this,
                        "本软件在 Operit AI 提供的工具支持下完成开发，代码编写与排查由 DeepSeek 模型辅助",
                        11, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        box.addView(UiKit.outlineButton(this, "项目主页　github.com/Nanheqiaobei/zisu",
                        UiKit.ACCENT, 0x552F6FED, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                openUrl("https://github.com/Nanheqiaobei/zisu");
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 4, 0, 8));

        box.addView(UiKit.outlineButton(this, "查看 GPL-3.0 协议全文", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new android.content.Intent(AboutActivity.this, LicenseActivity.class));
                    }
                }), UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 4, 0, 0));

        UiKit.card(root, box, this);
    }

    private TextView centered(String s, float sp, int color, boolean bold) {
        return UiKit.label(this, s, sp, color, bold, Gravity.CENTER);
    }

    /** 用浏览器打开一个链接，失败就忽略 */
    private void openUrl(String url) {
        try {
            startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)));
        } catch (Exception ignored) {
        }
    }

    private android.graphics.drawable.Drawable appIcon() {
        try {
            int id = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
            if (id != 0) {
                return getResources().getDrawable(id);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String versionName() {
        try {
            PackageInfo p = getPackageManager().getPackageInfo(getPackageName(), 0);
            return p.versionName == null ? "" : p.versionName;
        } catch (Exception e) {
            return "";
        }
    }
}
// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 首次启动的用户协议与隐私政策页。已同意则直接进主界面，之后不再出现。
 * 必须停留满 5 秒，「同意并继续」才可点。
 */
public class AgreementActivity extends Activity {

    private static final int WAIT_SECONDS = 5;

    private Store store;
    private final android.os.Handler ui =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private TextView agreeBtn;
    private int left = WAIT_SECONDS;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        if (store.agreed()) {
            goMain();
            return;
        }
        UiKit.boot(this, store);
        buildUi();
    }

    private void goMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void buildUi() {
        LinearLayout root = UiKit.column(this);
        UiKit.paintTransparent(root);
        root.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 22),
                UiKit.dp(this, 18), UiKit.dp(this, 18));

        root.addView(UiKit.label(this, "用户协议与隐私政策", 20, UiKit.TEXT, true, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 6));
        root.addView(UiKit.label(this, "使用前请阅读并同意以下内容", 12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        root.addView(buildLangSwitch(),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 16));

        root.addView(cardOf("关于本软件", new String[]{
                "本软件是一个实验性的 AI 自我演化工具，供个人研究、学习与娱乐使用，"
                        + "属于初级娱乐性质的技术演示。",
                "本软件不是角色扮演服务，不提供任何角色扮演内容，也不提供虚拟人物陪伴、"
                        + "情感陪聊等经营性服务。软件中的对话对象是一个 AI 程序，其全部输出由"
                        + "第三方大模型自动生成，不代表作者的观点或立场。",
                "本软件不面向未成年人提供任何形式的付费或经营服务。"
        }));

        root.addView(cardOf("用户协议", new String[]{
                "1. 本软件为自由软件，依据 GNU 通用公共许可证第 3 版（GPL-3.0）发布，"
                        + "你可以自由使用、修改和再分发；再分发时须以相同许可证开放源代码。",
                "2. 你须自行遵守所在国家或地区的法律法规以及你所接入服务的条款，"
                        + "不得利用本软件从事任何违法或侵害他人合法权益的行为，"
                        + "包括但不限于生成、传播违法违规信息。",
                "3. 本软件需接入第三方大模型服务方能对话。第三方服务产生的账号、数据与费用问题，"
                        + "与作者无关；接口调用产生的费用由你自行承担。",
                "4. 你通过本软件输入、生成或传播的内容，由你自行负责。",
                "5. 本软件按“现状”提供，不提供任何形式的担保；因使用或无法使用本软件造成的"
                        + "任何直接或间接损失，作者不承担责任。"
        }));

        root.addView(cardOf("隐私政策", new String[]{
                "1. 本软件不收集、不上传你的任何个人信息，也不含任何统计、广告或跟踪组件。",
                "2. 你的对话内容、设置、记忆等数据仅保存在本机应用私有目录；"
                        + "删除应用或数据即不可恢复，请自行做好备份。",
                "3. 你填写的接口地址与 API Key 仅保存在本机，用于向你指定的服务发起请求。",
                "4. 对话时，你发送的内容会经网络直接发送给你所配置的第三方大模型服务商，"
                        + "由该服务商按其隐私政策处理，作者无法获取。",
                "5. 以上为要点摘要。"
        }));

        // 按钮区：同意（满 5 秒可点）与不同意并退出
        agreeBtn = UiKit.button(this, "", 0xFFFFFFFF, UiKit.ACCENT, 14,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        store.setAgreed(true);
                        goMain();
                    }
                });
        agreeBtn.setEnabled(false);
        agreeBtn.setAlpha(0.45f);
        root.addView(agreeBtn, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 16, 0, 0));

        TextView reject = UiKit.outlineButton(this, "不同意并退出", UiKit.TEXT_SUB, UiKit.BORDER, 14,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finishAffinity();
                    }
                });
        root.addView(reject, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));

        root.addView(UiKit.label(this, "同意即表示你已阅读并接受上述用户协议与隐私政策",
                        11, UiKit.TEXT_SUB, false, Gravity.CENTER),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 12, 0, 0));

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 22), root, UiKit.dp(this, 18), true);
        UiKit.pageIn(root);

        startCountdown();
    }

    /** 顶部语言切换：中文 / English 两个胶囊，点一下换语言并重建 */
    private View buildLangSwitch() {
        LinearLayout row = UiKit.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(UiKit.label(this, "切换语言", 12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 12, 0));
        row.addView(langPill("中文", Lang.ZH));
        row.addView(langPill("English", Lang.EN),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 8, 0, 0, 0));
        return row;
    }

    private TextView langPill(String name, final int mode) {
        final boolean on = (store.langMode() == mode);
        TextView tv = UiKit.label(this, name, 12.5f, on ? 0xFFFFFFFF : UiKit.TEXT, on, Gravity.CENTER);
        tv.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 7), UiKit.dp(this, 14), UiKit.dp(this, 7));
        tv.setBackground(UiKit.shape(this, on ? UiKit.ACCENT : UiKit.CHIP_BG,
                on ? 0 : UiKit.BORDER, 20));
        tv.setClickable(true);
        UiKit.pressable(tv);
        tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                store.setLangMode(mode);
                recreate();
            }
        });
        return tv;
    }

    /** 一段带标题的协议卡片 */
    private View cardOf(String title, String[] lines) {
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 16), UiKit.dp(this, 16), UiKit.dp(this, 16));
        box.addView(UiKit.label(this, title, 13, UiKit.TEXT, true, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 8));
        for (int i = 0; i < lines.length; i++) {
            box.addView(UiKit.label(this, lines[i], 12, UiKit.TEXT, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
        }
        LinearLayout wrap = UiKit.column(this);
        UiKit.card(wrap, box, this);
        wrap.setPadding(0, 0, 0, UiKit.dp(this, 12));
        return wrap;
    }

    /** 满 5 秒才放行「同意」 */
    private void startCountdown() {
        tick();
    }

    private void tick() {
        if (left <= 0) {
            if (agreeBtn != null) {
                agreeBtn.setText(Lang.t("同意并继续"));
                agreeBtn.setEnabled(true);
                agreeBtn.setAlpha(1f);
            }
            return;
        }
        if (agreeBtn != null) {
            agreeBtn.setText(Lang.isEn() ? ("Please read " + left + "s") : (Lang.t("请阅读协议 ") + left + Lang.t(" 秒")));
        }
        ui.postDelayed(new Runnable() {
            public void run() {
                left--;
                tick();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
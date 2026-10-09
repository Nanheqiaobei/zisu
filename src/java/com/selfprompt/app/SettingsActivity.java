// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private Store store;
    private TextView modelConfigStatus;

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
        top.addView(UiKit.label(this, "设置", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 18));

        // ============ 模型配置入口 ============
        LinearLayout mc = UiKit.column(this);
        mc.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        mc.addView(UiKit.sectionTitle(this, "模型配置"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        mc.addView(UiKit.label(this,
                        "接口（Base URL、API Key、模型名、温度、top_p、最大输出、多套配置的切换）"
                                + "和「功能模型配置」都收进了这里",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        modelConfigStatus = sub("");
        mc.addView(modelConfigStatus, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 12));
        mc.addView(UiKit.button(this, "打开模型配置", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startActivity(new Intent(SettingsActivity.this,
                                        ModelConfigActivity.class));
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, mc, this);

        // ============ 对话设置入口 ============
        LinearLayout chat = UiKit.column(this);
        chat.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        chat.addView(UiKit.sectionTitle(this, "对话设置"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        chat.addView(UiKit.label(this,
                        "聊天方式（气泡分段式 / 普通式）和分段规则都在这里。"
                                + "思考强度不在这儿了，对话页输入框上面那个「思考」按钮上就能点",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        chat.addView(sub(Lang.t("当前聊天方式：") + (store.chatMode() == Store.CHAT_BUBBLE
                        ? "气泡分段式" : "普通式")),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        chat.addView(UiKit.button(this, "打开对话设置", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startActivity(new Intent(SettingsActivity.this,
                                        ChatSettingsActivity.class));
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, chat, this);

        // ============ 外观设置入口 ============
        LinearLayout look = UiKit.column(this);
        look.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        look.addView(UiKit.sectionTitle(this, "外观设置"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        look.addView(UiKit.label(this,
                        "主题、按钮与强调色的几种预设和自定义、背景图（可调清晰度与高斯模糊）、"
                                + "磨砂玻璃的开关和透明度、界面动画、自定义字体，都收进了这里",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        LinearLayout lookRow = UiKit.row(this);
        lookRow.setGravity(Gravity.CENTER_VERTICAL);
        lookRow.addView(UiKit.dot(this, UiKit.ACCENT, 16));
        lookRow.addView(UiKit.label(this, Lang.t("当前强调色 ") + Store.hexOf(UiKit.ACCENT)
                                + (store.frosted() ? Lang.t("　磨砂玻璃 · 开") : "")
                                + (store.bgPath().isEmpty() ? "" : Lang.t("　有背景图")),
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, this, 10, 0, 0, 0));
        look.addView(lookRow, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 12));
        look.addView(UiKit.button(this, "打开外观设置", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startActivity(new Intent(SettingsActivity.this,
                                        AppearanceActivity.class));
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, look, this);

        // ============ 记忆处理 ============
        LinearLayout ctx = UiKit.column(this);
        ctx.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
        ctx.addView(UiKit.sectionTitle(this, "记忆处理"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        ctx.addView(UiKit.label(this,
                        "对话攒到一定量时，后台自动提炼记忆（不影响聊天）："
                                + "海马体从对话里提炼原始信息，颞叶分类归档进「脑」的记忆流",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));
        addNumRow(ctx, "模型最大上下文长度", "tokens", String.valueOf(store.memLimit()),
                new NumCallback() {
                    public void onNum(int v) {
                        store.setMemLimit(v);
                    }
                });
        addNumRow(ctx, "触发比例", "%", String.valueOf(store.memRatio()),
                new NumCallback() {
                    public void onNum(int v) {
                        store.setMemRatio(v);
                    }
                });
        addNumRow(ctx, "前额叶看最近", "条对话", String.valueOf(store.routeWindow()),
                new NumCallback() {
                    public void onNum(int v) {
                        store.setRouteWindow(v);
                    }
                });
        addNumRow(ctx, "记忆整理后仅保留上下文轮数", "轮", String.valueOf(store.keepAfterTidy()),
                new NumCallback() {
                    public void onNum(int v) {
                        store.setKeepAfterTidy(v);
                    }
                });
        ctx.addView(UiKit.label(this,
                        "例：上限 128000 tokens、比例 70%，则上下文用到约 89600 tokens 时触发一次记忆整理；"
                                + "整理完成后，上下文只保留上面设定的轮数（前面的对话挪进前代记录）",
                        11.5f, 0xFFA6ADB4, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 6, 0, 0));
        UiKit.card(root, ctx, this);

        // ============ 数据 ============
        LinearLayout data = UiKit.column(this);
        data.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        data.addView(UiKit.sectionTitle(this, "数据"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        data.addView(UiKit.label(this,
                        "备份、还原、单独导出配置、单独导出角色，还有清空对话，都收进了这里",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        data.addView(UiKit.button(this, "打开数据", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startActivity(new Intent(SettingsActivity.this, DataActivity.class));
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, data, this);

        // ============ 语言 ============
        LinearLayout lang = UiKit.column(this);
        lang.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        lang.addView(UiKit.sectionTitle(this, "语言"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        lang.addView(UiKit.label(this, "界面语言只影响本软件的显示，不影响对话内容",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        lang.addView(UiKit.button(this, "打开语言设置", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startActivity(new Intent(SettingsActivity.this,
                                        LanguageActivity.class));
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, lang, this);

        // ============ 关于 ============
        buildAbout(root);

        root.addView(UiKit.label(this,
                "自我提示词不在这里改，改它的权限只属于 AI 自己。切换配置后回到对话页，历史会按新配置重排",
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

        refreshModelLine();
    }

    /** 关于：只放一个入口，点进去才看内容 */
    private void buildAbout(LinearLayout root) {
        LinearLayout about = UiKit.column(this);
        about.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 16));
        about.addView(UiKit.sectionTitle(this, "关于"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 10));
        about.addView(UiKit.label(this, "作者、版本，还有这个软件的初心",
                        12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        about.addView(UiKit.button(this, "打开关于", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.card(root, about, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshModelLine();
    }

    private void refreshModelLine() {
        if (modelConfigStatus == null) {
            return;
        }
        modelConfigStatus.setText(Lang.t("当前：") + store.activeName() + " · " + store.model()
                + (store.apiKey().isEmpty() ? Lang.t(" · 未配置 key") : ""));
    }

    private TextView sub(String s) {
        return UiKit.label(this, s, 11.5f, 0xFFA6ADB4, false, Gravity.START);
    }

    private interface NumCallback {
        void onNum(int v);
    }

    /** 一行设置：标签 + 当前值按钮，点开输数字 */
    private void addNumRow(LinearLayout parent, final String label, final String unit,
                           final String value, final NumCallback cb) {
        LinearLayout row = UiKit.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(UiKit.label(this, label, 13, UiKit.TEXT, false, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(UiKit.smallButton(this, value + " " + unit, UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        askNum(label, unit, value, cb);
                    }
                }));
        parent.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
    }

    private void askNum(final String label, String unit, String value, final NumCallback cb) {
        final android.widget.EditText input = UiKit.field(this, label, value);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        UiKit.dialog(this)
                .setTitle(label + "（" + unit + "）")
                .setView(input)
                .setPositiveButton(Lang.t("保存"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        try {
                            int v = Integer.parseInt(input.getText().toString().trim());
                            cb.onNum(v);
                            Toast.makeText(SettingsActivity.this, Lang.t("已保存"), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(SettingsActivity.this, Lang.t("请输入数字"), Toast.LENGTH_SHORT).show();
                        }
                        recreate();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }
}
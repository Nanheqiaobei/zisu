// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** 模型配置：接口那一段，加上「功能模型配置」。从设置页进来 */
public class ModelConfigActivity extends Activity {

    private Store store;
    private EditText base;
    private EditText key;
    private EditText model;
    private EditText tempField;
    private EditText topPField;
    private EditText tokensField;

    private TextView profileLabel;
    private TextView balanceStatus;
    private TextView modelStatus;

    private TextView fnSelfStatus;
    private TextView fnSelfPrompt;
    /** 脑区三个功能模型的提示词标签，统一刷新 */
    private final List<TextView> fnBrainPromptViews = new ArrayList<TextView>();
    private final List<String> fnBrainPromptKeys = new ArrayList<String>();

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
        top.addView(UiKit.label(this, "模型配置", 20, UiKit.TEXT, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 18));

        // ============ 接口 ============
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));

        LinearLayout head = UiKit.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(UiKit.sectionTitle(this, "接口"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(UiKit.smallButton(this, "新建配置", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        newProfile();
                    }
                }));
        head.addView(UiKit.smallButton(this, "切换", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        switchProfile();
                    }
                }), UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        card.addView(head);

        profileLabel = UiKit.label(this, "", 12.5f, UiKit.TEXT_SUB, false, Gravity.START);
        card.addView(profileLabel, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 8, 0, 14));

        card.addView(fieldLabel("Base URL"));
        base = UiKit.field(this, Store.DEF_BASE, store.baseUrl());
        card.addView(base, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
        card.addView(sub("填到版本号为止，例如 https://api.deepseek.com 或 https://api.openai.com/v1，程序会自动补 /chat/completions"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 6, 0, 16));

        card.addView(labelRowWithButton("API Key", "管理", new View.OnClickListener() {
            public void onClick(View v) {
                showKeyDialog();
            }
        }));
        key = UiKit.field(this, "sk-…", store.apiKey());
        card.addView(key, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
        // 默认星号显示，聚焦时才明文，方便截图
        key.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        key.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                key.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                        | (hasFocus ? 0 : android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD));
                key.setSelection(key.getText().length());
                if (!hasFocus && key.getText().toString().trim().length() > 0) {
                    autoQuery();
                }
            }
        });

        card.addView(labelRowWithButton("账户余额", "查询余额", new View.OnClickListener() {
            public void onClick(View v) {
                queryBalance();
            }
        }), UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 14, 0, 4));
        balanceStatus = sub("尚未查询");
        card.addView(balanceStatus);

        card.addView(labelRowWithButton("模型名", "获取模型", new View.OnClickListener() {
            public void onClick(View v) {
                fetchModels(true);
            }
        }), UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 16, 0, 4));
        model = UiKit.field(this, Store.DEF_MODEL, store.model());
        card.addView(model, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 2, 0, 16));

        card.addView(fieldLabel("采样温度（0 到 2，默认 0.9）"));
        tempField = UiKit.field(this, "0.9", String.valueOf(store.tempOf(store.activeIndex())));
        card.addView(tempField, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 0));
        card.addView(sub("越大越随机，越小越死板。对话建议 0.7 到 1.0"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 6, 0, 16));

        card.addView(fieldLabel("top_p（0.95 到 1.0，思考模式下温度不生效，用这个）"));
        topPField = UiKit.field(this, "1.0", String.valueOf(store.topPOf(store.activeIndex())));
        card.addView(topPField, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 16));

        card.addView(fieldLabel("最大输出 tokens（0 表示不限制）"));
        tokensField = UiKit.field(this, "0", String.valueOf(store.maxTokensOf(store.activeIndex())));
        card.addView(tokensField, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 18));
        modelStatus = sub("填好 key 之后会自动获取可用模型");
        card.addView(modelStatus, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 18));

        card.addView(UiKit.button(this, "保存", 0xFFFFFFFF, UiKit.ACCENT, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                save();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));

        card.addView(UiKit.outlineButton(this, "删除当前配置", UiKit.DANGER, 0x33C0392B, 12,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                deleteProfile();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 10, 0, 0));
        UiKit.card(root, card, this);

        // ============ 功能模型配置 ============
        LinearLayout fn = UiKit.column(this);
        fn.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
        fn.addView(UiKit.sectionTitle(this, "功能模型配置"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));
        fn.addView(UiKit.label(this,
                        "这两件事可以交给别的配置去做，选的就是上面那套模型配置。不选就跟当前配置一致。"
                                + "「自定义提示词」用来改这两件事自己用的提示词，改错了可以一键恢复默认",
                        12, UiKit.TEXT_SUB, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 14));

        LinearLayout selfRow = UiKit.row(this);
        selfRow.setGravity(Gravity.CENTER_VERTICAL);
        selfRow.addView(UiKit.label(this, "自我设定修改", 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        selfRow.addView(UiKit.smallButton(this, "自定义提示词", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        editFnPrompt(Store.FN_SELF, "自我设定修改的提示词");
                    }
                }));
        selfRow.addView(UiKit.smallButton(this, "选择", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                pickFunctionProfile(Store.FN_SELF, fnSelfStatus);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        fn.addView(selfRow);
        fnSelfStatus = sub(store.fnLabel(Store.FN_SELF));
        fn.addView(fnSelfStatus, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 6, 0, 2));
        fnSelfPrompt = sub(store.fnPromptLabel(Store.FN_SELF));
        fn.addView(fnSelfPrompt, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 16));

        // 脑区三个：海马体 / 前额叶 / 颞叶
        addBrainFnBlock(fn, Store.FN_HIPPOCAMPUS, "海马体",
                "管记忆的写入与巩固，用独立上下文把该记住的事提炼成文件写进「脑」，不受当前对话干扰", 16);
        addBrainFnBlock(fn, Store.FN_PREFRONTAL, "前额叶",
                "管调度与习惯，决定每轮从「脑」取哪几段放进上下文，也把反复出现的流程固化成文件", 16);
        addBrainFnBlock(fn, Store.FN_TEMPORAL, "颞叶",
                "管整理与轻重，把知识归类存放，给每条记忆标重要度，越重要的越常被取用", 0);
        UiKit.card(root, fn, this);

        root.addView(UiKit.label(this,
                "功能模型配置里的选择是立刻生效的，不用点保存。换配置后回到对话页，历史会按新配置重排",
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

        refreshFields();
        if (store.apiKey().length() > 0) {
            autoQuery();
        }
    }

    // ============ 配置 ============

    private void refreshFields() {
        profileLabel.setText(Lang.t("当前配置：") + store.activeName()
                + Lang.t("（共 ") + store.profileCount() + Lang.t(" 套，点右上角可新建或切换）"));
        base.setText(store.baseUrl());
        key.setText(store.apiKey());
        model.setText(store.model());
    }

    private void switchProfile() {
        final int n = store.profileCount();
        final String[] names = new String[n];
        for (int i = 0; i < n; i++) {
            names[i] = store.profileName(i);
        }
        showListDialog("切换配置", names, store.activeIndex(), new PickCallback() {
            public void onPick(int which) {
                store.setActive(which);
                refreshFields();
                balanceStatus.setText(Lang.t("尚未查询"));
                modelStatus.setText(Lang.t("填好 key 之后会自动获取可用模型"));
                if (store.apiKey().length() > 0) {
                    autoQuery();
                }
            }
        });
    }

    /** 列表项选中回调 */
    private interface PickCallback {
        void onPick(int index);
    }

    /**
     * 自定义的紧凑列表弹窗。系统 setItems 的行高和间距都大，模型多的时候一屏放不下几个；
     * 这里自己排，行矮一点、选中项淡蓝底，整个弹窗也收窄一些。
     */
    private void showListDialog(String title, String[] items, int curIdx, final PickCallback cb) {
        final AlertDialog[] holder = new AlertDialog[1];
        LinearLayout col = UiKit.column(this);
        col.setPadding(UiKit.dp(this, 8), UiKit.dp(this, 4), UiKit.dp(this, 8), UiKit.dp(this, 4));
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            boolean on = i == curIdx;
            TextView t = UiKit.label(this, items[i], 13.5f, on ? UiKit.ACCENT : UiKit.TEXT,
                    on, Gravity.START);
            t.setPadding(UiKit.dp(this, 14), UiKit.dp(this, 9), UiKit.dp(this, 14), UiKit.dp(this, 9));
            t.setBackground(UiKit.shape(this, on ? UiKit.ACCENT_SOFT : 0x00000000, 0, 10));
            t.setClickable(true);
            UiKit.pressable(t);
            t.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (holder[0] != null) {
                        holder[0].dismiss();
                    }
                    if (cb != null) {
                        cb.onPick(idx);
                    }
                }
            });
            col.addView(t, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 3));
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(col, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // 收窄一点，别铺满整屏；高度封顶，模型多了里面滚
        int h = Math.min(UiKit.dp(this, 300), UiKit.dp(this, 46) * items.length + UiKit.dp(this, 8));
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                UiKit.dp(this, 240), Math.max(UiKit.dp(this, 96), h)));
        AlertDialog d = UiKit.dialog(this)
                .setTitle(title)
                .setView(sv)
                .setNegativeButton(Lang.t("取消"), null)
                .create();
        holder[0] = d;
        d.show();
    }

    private void newProfile() {
        final EditText input = UiKit.field(this, "给这套配置起个名字", "");
        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 8), UiKit.dp(this, 16), UiKit.dp(this, 4));
        box.addView(input, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
        UiKit.dialog(this)
                .setTitle(Lang.t("新建配置"))
                .setView(box)
                .setPositiveButton(Lang.t("创建"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.createProfile(input.getText().toString());
                        refreshFields();
                        Toast.makeText(ModelConfigActivity.this, Lang.t("已新建并切换到新配置"),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void deleteProfile() {
        if (store.profileCount() <= 1) {
            Toast.makeText(this, Lang.t("只剩一套配置，不能删除"), Toast.LENGTH_SHORT).show();
            return;
        }
        UiKit.dialog(this)
                .setTitle(Lang.t("删除配置"))
                .setMessage(Lang.t("会删除「") + store.activeName() + Lang.t("」这套接口配置，已保存的 key 仍在 key 池里"))
                .setPositiveButton(Lang.t("删除"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.deleteActiveProfile();
                        refreshFields();
                        Toast.makeText(ModelConfigActivity.this, Lang.t("已删除"), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    private void showKeyDialog() {
        final LinearLayout col = UiKit.column(this);
        col.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 4), UiKit.dp(this, 18), UiKit.dp(this, 4));
        final AlertDialog dlg = UiKit.dialog(this)
                .setTitle(Lang.t("管理 key（只属于当前配置）"))
                .setView(col)
                .setNegativeButton(Lang.t("关闭"), null)
                .create();
        fillKeyRows(col, dlg);
        dlg.show();
    }

    private void fillKeyRows(final LinearLayout col, final AlertDialog dlg) {
        col.removeAllViews();
        JSONArray arr = store.savedKeys();
        final List<String> keys = new ArrayList<String>();
        for (int i = 0; i < arr.length(); i++) {
            String k = arr.optString(i, "");
            if (!k.isEmpty()) {
                keys.add(k);
            }
        }
        if (keys.isEmpty()) {
            col.addView(UiKit.label(this, "这套配置下还没有保存过 key", 13,
                    UiKit.TEXT_SUB, false, Gravity.START));
            return;
        }
        String cur = key.getText().toString().trim();
        for (int i = 0; i < keys.size(); i++) {
            final int idx = i;
            final String k = keys.get(i);
            LinearLayout r = UiKit.row(this);
            r.setGravity(Gravity.CENTER_VERTICAL);

            TextView t = UiKit.label(this,
                    mask(k) + (k.equals(cur) ? "（当前使用）" : ""),
                    13.5f, UiKit.TEXT, false, Gravity.START);
            t.setClickable(true);
            t.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    key.setText(k);
                    dlg.dismiss();
                }
            });
            r.addView(t, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            r.addView(UiKit.smallButton(this, "删除", UiKit.DANGER, 0x1FC0392B, 10,
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            store.removeSavedKeyAt(idx);
                            fillKeyRows(col, dlg);
                            Toast.makeText(ModelConfigActivity.this, Lang.t("已删除该 key"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }));
            col.addView(r, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 12));
        }
    }

    private String mask(String k) {
        if (k.length() <= 12) {
            return k;
        }
        return k.substring(0, 6) + "…" + k.substring(k.length() - 4);
    }

    // ============ 余额与模型 ============

    private void autoQuery() {
        queryBalance();
        fetchModels(false);
    }

    private void queryBalance() {
        final String u = base.getText().toString().trim();
        final String k = key.getText().toString().trim();
        if (u.isEmpty() || k.isEmpty()) {
            balanceStatus.setText(Lang.t("Base URL 和 API Key 都填上才能查询"));
            return;
        }
        balanceStatus.setText(Lang.t("查询中…"));
        ApiTools.balance(u, k, new ApiTools.TextCallback() {
            public void onResult(final String text, final boolean ok) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        balanceStatus.setText(text);
                        balanceStatus.setTextColor(ok ? 0xFF3C7A3C : 0xFFB26B00);
                    }
                });
            }
        });
    }

    private void fetchModels(final boolean showDialog) {
        final String u = base.getText().toString().trim();
        final String k = key.getText().toString().trim();
        if (u.isEmpty() || k.isEmpty()) {
            modelStatus.setText(Lang.t("Base URL 和 API Key 都填上才能获取"));
            return;
        }
        modelStatus.setText(showDialog ? Lang.t("获取中…") : Lang.t("自动获取模型中…"));
        ApiTools.modelIds(u, k, new ApiTools.TextCallback() {
            public void onResult(final String text, final boolean ok) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (!ok) {
                            modelStatus.setText(text);
                            modelStatus.setTextColor(0xFFB26B00);
                            return;
                        }
                        List<String> ids = new ArrayList<String>();
                        String[] lines = text.split("\n");
                        for (int i = 0; i < lines.length; i++) {
                            String s = lines[i].trim();
                            if (!s.isEmpty()) {
                                ids.add(s);
                            }
                        }
                        modelStatus.setTextColor(UiKit.TEXT_SUB);
                        modelStatus.setText(Lang.t("可用模型：") + join(ids) + Lang.t("（点“获取模型”切换）"));
                        if (model.getText().toString().trim().isEmpty() && !ids.isEmpty()) {
                            model.setText(ids.get(0));
                        }
                        if (showDialog && !ids.isEmpty()) {
                            pickModel(ids);
                        }
                    }
                });
            }
        });
    }

    private void pickModel(final List<String> ids) {
        final String[] items = ids.toArray(new String[ids.size()]);
        String cur = model.getText().toString().trim();
        int curIdx = -1;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(cur)) {
                curIdx = i;
                break;
            }
        }
        showListDialog("选择模型", items, curIdx, new PickCallback() {
            public void onPick(int which) {
                model.setText(items[which]);
            }
        });
    }

    private String join(List<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            sb.append(ids.get(i));
        }
        return sb.toString();
    }

    // ============ 功能模型 ============

    private void pickFunctionProfile(final String fn, final TextView status) {
        final int n = store.profileCount();
        final String[] items = new String[n + 1];
        items[0] = "跟当前配置一致";
        for (int i = 0; i < n; i++) {
            items[i + 1] = store.profileName(i);
        }
        showListDialog("选择模型配置", items, store.functionProfile(fn) + 1, new PickCallback() {
            public void onPick(int which) {
                store.setFunctionProfile(fn, which - 1);
                status.setText(store.fnLabel(fn));
            }
        });
    }

    /** 改这件事自己用的提示词。留空即恢复内置默认 */
    private void editFnPrompt(final String fn, String title) {
        final EditText input = UiKit.field(this, "留空表示用内置默认", store.fnPromptOrDefault(fn));
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(8);
        input.setMaxLines(14);
        input.setTextSize(13);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        // 弹窗主题跟着深色模式走，这里的颜色也跟主题
        input.setTextColor(UiKit.TEXT);
        input.setHintTextColor(UiKit.TEXT_SUB);
        input.setBackground(UiKit.shape(this, UiKit.INPUT_BG, UiKit.BORDER, 14));

        LinearLayout box = UiKit.column(this);
        box.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 4), UiKit.dp(this, 16), UiKit.dp(this, 4));
        TextView tip = UiKit.label(this, "发给模型的 system 内容，改的是这件事怎么做，不是它的自我设定",
                11.5f, 0xFF8A9099, false, Gravity.START);
        box.addView(tip, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 8));
        box.addView(input, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));

        UiKit.dialog(this)
                .setTitle(title)
                .setView(box)
                .setPositiveButton(Lang.t("保存"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        String t = input.getText().toString().trim();
                        store.setFnPrompt(fn, t);
                        refreshFnPromptLabels();
                        Toast.makeText(ModelConfigActivity.this,
                                t.isEmpty() ? Lang.t("已改回内置默认") : Lang.t("已保存自定义提示词"),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(Lang.t("恢复默认"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        store.setFnPrompt(fn, "");
                        refreshFnPromptLabels();
                        Toast.makeText(ModelConfigActivity.this, Lang.t("已改回内置默认"),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("取消"), null)
                .show();
    }

    /** 脑区功能模型的一行：名字 + 小字解释 + 自定义提示词 + 选择配置 */
    private void addBrainFnBlock(LinearLayout parent, final String fn, String name,
                                 String explain, int bottomDp) {
        parent.addView(UiKit.label(this, name, 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 12, 0, 2));
        parent.addView(UiKit.label(this, explain, 11.5f, UiKit.NOTE_TEXT, false, Gravity.START),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 6));
        LinearLayout row = UiKit.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(UiKit.label(this, Lang.t("配置"), 12, UiKit.TEXT_SUB, false, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final TextView status = sub(store.fnLabel(fn));
        row.addView(UiKit.smallButton(this, Lang.t("自定义提示词"), UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        editFnPrompt(fn, Lang.t("功能模型的提示词"));
                    }
                }));
        row.addView(UiKit.smallButton(this, Lang.t("选择"), UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                pickFunctionProfile(fn, status);
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        parent.addView(row, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 4));
        parent.addView(status, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 2));
        TextView prompt = sub(store.fnPromptLabel(fn));
        parent.addView(prompt, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, bottomDp));
        fnBrainPromptViews.add(prompt);
        fnBrainPromptKeys.add(fn);
    }

    private void refreshFnPromptLabels() {
        if (fnSelfPrompt != null) {
            fnSelfPrompt.setText(store.fnPromptLabel(Store.FN_SELF));
        }
        for (int i = 0; i < fnBrainPromptViews.size(); i++) {
            fnBrainPromptViews.get(i).setText(store.fnPromptLabel(fnBrainPromptKeys.get(i)));
        }
    }

    // ============ 保存 ============

    private void save() {
        float temp = 0.9f;
        try {
            temp = Float.parseFloat(tempField.getText().toString().trim());
        } catch (Exception ignored) {
        }
        if (temp < 0f) {
            temp = 0f;
        }
        if (temp > 2f) {
            temp = 2f;
        }
        int tokens = 0;
        try {
            tokens = Integer.parseInt(tokensField.getText().toString().trim());
        } catch (Exception ignored) {
        }
        if (tokens < 0) {
            tokens = 0;
        }
        float topP = 1f;
        try {
            topP = Float.parseFloat(topPField.getText().toString().trim());
        } catch (Exception ignored) {
        }
        if (topP < 0.01f) {
            topP = 1f;
        }
        if (topP > 1f) {
            topP = 1f;
        }
        store.saveActiveConfig(base.getText().toString(), key.getText().toString(),
                model.getText().toString(), temp, topP, tokens);
        Toast.makeText(this, Lang.t("已保存"), Toast.LENGTH_SHORT).show();
        finish();
    }

    private LinearLayout labelRowWithButton(String name, String btn, View.OnClickListener l) {
        LinearLayout r = UiKit.row(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.addView(UiKit.label(this, name, 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(UiKit.smallButton(this, btn, UiKit.ACCENT, UiKit.ACCENT_SOFT, 10, l));
        return r;
    }

    private TextView fieldLabel(String s) {
        return UiKit.label(this, s, 12.5f, UiKit.TEXT_SUB, true, Gravity.START);
    }

    private TextView sub(String s) {
        return UiKit.label(this, s, 11.5f, 0xFFA6ADB4, false, Gravity.START);
    }
}

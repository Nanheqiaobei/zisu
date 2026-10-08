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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** 资料编辑页：使用者这一侧写名字和描述，AI 那一侧换头像 */
public class ProfileActivity extends Activity {

    /** which=user 编辑使用者，which=ai 只换 AI 头像并给提示词入口 */
    public static final String EXTRA_WHICH = "which";
    private static final int REQ_AVATAR = 3001;
    private static final int REQ_CROP = 3002;
    private String pendingRaw = "";

    private Store store;
    private String which = "user";
    private TextView avatarStatus;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        UiKit.boot(this, store);
        which = getIntent() == null ? "user" : getIntent().getStringExtra(EXTRA_WHICH);
        if (which == null) {
            which = "user";
        }
        final boolean isUser = "user".equals(which);

        LinearLayout root = UiKit.column(this);
        UiKit.paintTransparent(root);
        root.setPadding(UiKit.dp(this, 16), UiKit.dp(this, 14), UiKit.dp(this, 16), UiKit.dp(this, 28));

        LinearLayout top = UiKit.row(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(UiKit.label(this, isUser ? "编辑我的资料" : "它的资料", 20, UiKit.TEXT, true,
                        Gravity.START),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(UiKit.outlineButton(this, "返回", UiKit.TEXT_SUB, UiKit.BORDER, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                }));
        root.addView(top, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 14));

        // 头像
        LinearLayout card = UiKit.column(this);
        card.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
        card.addView(UiKit.sectionTitle(this, "头像"),
                UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f, this, 0, 0, 0, 12));

        LinearLayout avRow = UiKit.row(this);
        avRow.setGravity(Gravity.CENTER_VERTICAL);
        String name = isUser ? store.userName() : store.coreName();
        String path = isUser ? store.userAvatar() : store.aiAvatar();
        avRow.addView(UiKit.avatarView(this, name, path, isUser, 56));
        avatarStatus = UiKit.label(this, path.isEmpty() ? "没有自定义头像，用首字" : "已设置自定义头像",
                12.5f, UiKit.TEXT_SUB, false, Gravity.START);
        avRow.addView(avatarStatus, UiKit.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f,
                this, 12, 0, 0, 0));
        card.addView(avRow);

        LinearLayout avBtns = UiKit.row(this);
        avBtns.addView(UiKit.smallButton(this, "选一张图片", UiKit.ACCENT, UiKit.ACCENT_SOFT, 10,
                new View.OnClickListener() {
                    public void onClick(View v) {
                        pickAvatar();
                    }
                }));
        avBtns.addView(UiKit.smallButton(this, "清除头像", UiKit.TEXT_SUB, UiKit.CHIP_BG, 10,
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                if (isUser) {
                                    store.setUserAvatar("");
                                } else {
                                    store.setAiAvatar("");
                                }
                                avatarStatus.setText(Lang.t("没有自定义头像，用首字"));
                                Toast.makeText(ProfileActivity.this, Lang.t("已清除，回对话页生效"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }),
                UiKit.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 8, 0, 0, 0));
        card.addView(avBtns, UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 14, 0, 0));
        UiKit.card(root, card, this);

        if (isUser) {
            LinearLayout info = UiKit.column(this);
            info.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
            info.addView(UiKit.sectionTitle(this, "资料"),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 12));
            info.addView(UiKit.label(this, "名字（显示在你头像旁边）", 12.5f, UiKit.TEXT_SUB, true, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 6));
            final EditText nameField = UiKit.field(this, "我", store.userName());
            nameField.setSingleLine(true);
            info.addView(nameField);

            info.addView(UiKit.label(this, "关于你（会一起发给它，帮它认识你）", 12.5f, UiKit.TEXT_SUB, true,
                            Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 16, 0, 6));
            final EditText descField = UiKit.field(this, "比如你怎么称呼、在意什么、不喜欢被怎么对待",
                    store.userDesc());
            descField.setMinLines(4);
            descField.setMaxLines(12);
            descField.setGravity(Gravity.TOP);
            info.addView(descField);

            info.addView(UiKit.button(this, "保存", 0xFFFFFFFF, UiKit.ACCENT, 12,
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    store.setUserName(nameField.getText().toString());
                                    store.setUserDesc(descField.getText().toString());
                                    Toast.makeText(ProfileActivity.this, Lang.t("已保存"), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 18, 0, 0));
            UiKit.card(root, info, this);
        } else {
            LinearLayout toPrompt = UiKit.column(this);
            toPrompt.setPadding(UiKit.dp(this, 18), UiKit.dp(this, 16), UiKit.dp(this, 18), UiKit.dp(this, 18));
            toPrompt.addView(UiKit.label(this,
                            "它的名字和它的自我设定都在提示词页里，名字只有你能改，设定由它那边维护",
                            12.5f, UiKit.TEXT_SUB, false, Gravity.START),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0f, this, 0, 0, 0, 14));
            toPrompt.addView(UiKit.button(this, "打开它的提示词", 0xFFFFFFFF, UiKit.ACCENT, 12,
                            new View.OnClickListener() {
                                public void onClick(View v) {
                                    startActivity(new Intent(ProfileActivity.this, PromptActivity.class));
                                }
                            }),
                    UiKit.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 0f, this, 0, 0, 0, 0));
            UiKit.card(root, toPrompt, this);
        }

        ScrollView sv = new ScrollView(this);
        UiKit.paintBackground(sv);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);

        UiKit.edgeToEdge(this, root, UiKit.dp(this, 14), root, UiKit.dp(this, 28), true);
        UiKit.pageIn(root);
    }

    private void pickAvatar() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        try {
            startActivityForResult(i, REQ_AVATAR);
        } catch (Exception e) {
            Toast.makeText(this, Lang.t("没有可用的图片选择器"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null) {
            return;
        }
        if (req == REQ_AVATAR) {
            if (data.getData() == null) {
                return;
            }
            try {
                File dir = new File(getFilesDir(), "avatars");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File raw = new File(dir, which + "_raw_" + System.currentTimeMillis() + ".img");
                InputStream is = getContentResolver().openInputStream(data.getData());
                FileOutputStream fos = new FileOutputStream(raw);
                byte[] buf = new byte[8192];
                int n;
                while (is != null && (n = is.read(buf)) > 0) {
                    fos.write(buf, 0, n);
                }
                if (is != null) {
                    is.close();
                }
                fos.close();
                pendingRaw = raw.getAbsolutePath();
                File out = new File(dir, which + "_" + System.currentTimeMillis() + ".png");
                Intent crop = new Intent(this, CropActivity.class);
                crop.putExtra(CropActivity.EXTRA_SRC, pendingRaw);
                crop.putExtra(CropActivity.EXTRA_DST, out.getAbsolutePath());
                startActivityForResult(crop, REQ_CROP);
            } catch (Exception e) {
                Toast.makeText(this, Lang.t("读图失败：") + e.getClass().getSimpleName(),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (req == REQ_CROP) {
            String path = data.getStringExtra(CropActivity.EXTRA_DST);
            if (path == null || path.isEmpty()) {
                return;
            }
            if ("user".equals(which)) {
                store.setUserAvatar(path);
            } else {
                store.setAiAvatar(path);
            }
            Toast.makeText(this, Lang.t("换好了，回对话页生效"), Toast.LENGTH_SHORT).show();
            recreate();
        }
    }
}
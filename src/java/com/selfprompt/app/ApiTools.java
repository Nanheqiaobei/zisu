// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** 接口侧的小工具：查询账户余额、拉取模型列表（OpenAI 兼容接口） */
public class ApiTools {

    public interface TextCallback {
        /** ok=false 表示失败，text 是给用户看的原因 */
        void onResult(String text, boolean ok);
    }

    public static String normalizeBase(String base) {
        String b = base == null ? "" : base.trim();
        while (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        return b;
    }

    /** DeepSeek 的余额接口：GET /user/balance */
    public static void balance(final String base, final String key, final TextCallback cb) {
        new Thread(new Runnable() {
            public void run() {
                String body = get(normalizeBase(base) + "/user/balance", key);
                if (body == null) {
                    cb.onResult("查询失败，可能这个接口不支持余额查询", false);
                    return;
                }
                try {
                    JSONObject o = new JSONObject(body);
                    JSONArray infos = o.optJSONArray("balance_infos");
                    if (infos == null || infos.length() == 0) {
                        cb.onResult("接口没有返回余额信息", false);
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < infos.length(); i++) {
                        JSONObject b = infos.getJSONObject(i);
                        if (i > 0) {
                            sb.append("；");
                        }
                        sb.append(Lang.t("可用 ")).append(b.optString("total_balance", "-"))
                          .append(" ").append(b.optString("currency", ""))
                          .append(Lang.t("（赠送 ")).append(b.optString("granted_balance", "-"))
                          .append(Lang.t("，充值 ")).append(b.optString("topped_up_balance", "-"))
                          .append(Lang.t("）"));
                    }
                    if (!o.optBoolean("is_available", true)) {
                        sb.append(Lang.t("　余额不足"));
                    }
                    cb.onResult(sb.toString(), true);
                } catch (Exception e) {
                    cb.onResult("返回内容解析失败：" + e.getClass().getSimpleName(), false);
                }
            }
        }).start();
    }

    /** 模型列表：GET /models */
    public static void modelIds(final String base, final String key, final TextCallback cb) {
        new Thread(new Runnable() {
            public void run() {
                String body = get(normalizeBase(base) + "/models", key);
                if (body == null) {
                    cb.onResult("获取失败，检查 Base URL 和 key", false);
                    return;
                }
                try {
                    JSONArray data = new JSONObject(body).optJSONArray("data");
                    if (data == null || data.length() == 0) {
                        cb.onResult("接口没有返回模型列表", false);
                        return;
                    }
                    List<String> ids = new ArrayList<String>();
                    for (int i = 0; i < data.length(); i++) {
                        String id = data.getJSONObject(i).optString("id", "");
                        if (!id.isEmpty()) {
                            ids.add(id);
                        }
                    }
                    if (ids.isEmpty()) {
                        cb.onResult("接口没有返回模型列表", false);
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ids.size(); i++) {
                        if (i > 0) {
                            sb.append("\n");
                        }
                        sb.append(ids.get(i));
                    }
                    cb.onResult(sb.toString(), true);
                } catch (Exception e) {
                    cb.onResult("返回内容解析失败：" + e.getClass().getSimpleName(), false);
                }
            }
        }).start();
    }

    private static String get(String url, String key) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Authorization", "Bearer " + key);
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String body = readAll(is);
            if (code < 200 || code >= 300) {
                return null;
            }
            return body;
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readAll(InputStream is) {
        if (is == null) {
            return "";
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            is.close();
            return new String(bos.toByteArray(), "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
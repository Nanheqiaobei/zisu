// Copyright (C) 2026 亦安一北
// SPDX-License-Identifier: GPL-3.0-or-later
// 本文件是「自塑」的一部分，依据 GNU 通用公共许可证第 3 版或更新版本发布，详见随附的 LICENSE。

package com.selfprompt.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 极简 OpenAI 兼容接口客户端，流式 (SSE) + function calling */
public class ChatClient {

    public interface Listener {
        /** 增量正文 */
        void onDelta(String text);

        /** 模型原生的思考内容（有些模型会返回 reasoning_content） */
        void onReasoning(String text);

        /** 流结束后按顺序回调完整的工具调用 */
        void onToolCall(String id, String name, String args);

        /** 结束；error 非 null 表示失败 */
        void onDone(String error);

        /** token 用量（部分接口返回；默认空实现，不强制实现类处理） */
        default void onUsage(int promptTokens, int completionTokens, int cacheHit, int cacheMiss) {
        }
    }

    public static class ToolCall {
        public String id = "";
        public String name = "";
        public String args = "";
    }

    private volatile boolean cancelled;

    public void cancel() {
        cancelled = true;
    }

    public void send(final String baseUrl, final String apiKey, final String model,
                     final JSONArray messages, final Listener listener) {
        send(baseUrl, apiKey, model, messages, listener, 0.9f, 1f, 0, null, false, "high");
    }

    /**
     * @param tools    这一轮允许它用的工具，传 null 或空数组表示不给工具
     * @param thinking 是否开启思考模式（DeepSeek 现在是请求参数控制，不是靠模型名）
     * @param effort   思考强度 none / low / high / max，仅在 thinking 为 true 时有意义
     */
    public void send(final String baseUrl, final String apiKey, final String model,
                     final JSONArray messages, final Listener listener,
                     final float temperature, final float topP, final int maxTokens,
                     final JSONArray tools, final boolean thinking, final String effort) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSend(baseUrl, apiKey, model, messages, listener, temperature, topP, maxTokens,
                        tools, thinking, effort);
            }
        }).start();
    }

    /** 看门狗：这么久还没有正常收尾，就当它挂了，主动断开并报错，绝不让界面一直等 */
    private static final long WATCHDOG_MS = 150000;

    private void doSend(String baseUrl, String apiKey, String model, JSONArray messages,
                        Listener listener, float temperature, float topP, int maxTokens,
                        JSONArray tools, boolean thinking, String effort) {
        HttpURLConnection conn = null;
        // onDone 只允许交付一次：看门狗、正常收尾、异常三条路互斥，免得重复触发整轮逻辑
        final java.util.concurrent.atomic.AtomicBoolean delivered =
                new java.util.concurrent.atomic.AtomicBoolean(false);
        final String[] timeoutMsg = new String[1];
        Thread watchdog = null;
        try {
            String base = baseUrl == null ? "" : baseUrl.trim();
            while (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            URL url = new URL(base + "/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(90000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            final HttpURLConnection liveConn = conn;
            watchdog = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(WATCHDOG_MS);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (delivered.get()) {
                        return;
                    }
                    timeoutMsg[0] = Lang.t("请求超时（") + (WATCHDOG_MS / 1000) + Lang.t(" 秒没有任何响应，已断开）");
                    cancelled = true;
                    try {
                        liveConn.disconnect();
                    } catch (Exception ignored) {
                    }
                    if (delivered.compareAndSet(false, true)) {
                        listener.onDone(timeoutMsg[0]);
                    }
                }
            });
            watchdog.setDaemon(true);
            watchdog.start();

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", messages);
            body.put("stream", true);
            body.put("temperature", temperature);
            body.put("top_p", topP);
            JSONObject thinkBody = new JSONObject();
            thinkBody.put("type", thinking ? "enabled" : "disabled");
            body.put("thinking", thinkBody);
            // 让 DeepSeek 在流末尾多带一个 usage 块（缓存命中/未命中都在这），用于上下文监测
            if (base.contains("deepseek")) {
                JSONObject so = new JSONObject();
                so.put("include_usage", true);
                body.put("stream_options", so);
            }
            if (thinking && effort != null && !effort.trim().isEmpty()) {
                body.put("reasoning_effort", effort.trim());
            }
            if (tools != null && tools.length() > 0) {
                body.put("tools", tools);
                body.put("tool_choice", "auto");
            }
            if (maxTokens > 0) {
                body.put("max_tokens", maxTokens);
            }

            byte[] raw = body.toString().getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(raw);
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            if (code != 200) {
                InputStream es = conn.getErrorStream();
                String msg = readAll(es);
                if (msg.length() > 500) {
                    msg = msg.substring(0, 500);
                }
                if (delivered.compareAndSet(false, true)) {
                    listener.onDone("HTTP " + code + " " + msg);
                }
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            Map<Integer, ToolCall> pending = new HashMap<Integer, ToolCall>();
            List<Integer> order = new ArrayList<Integer>();
            while ((line = br.readLine()) != null) {
                if (cancelled) {
                    break;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty()) {
                    continue;
                }
                if ("[DONE]".equals(data)) {
                    break;
                }
                JSONObject o = new JSONObject(data);
                // usage 块：通常出现在流的最后（choices 为空），先把用量收下
                JSONObject usage = o.optJSONObject("usage");
                if (usage != null) {
                    listener.onUsage(
                            usage.optInt("prompt_tokens", 0),
                            usage.optInt("completion_tokens", 0),
                            usage.optInt("prompt_cache_hit_tokens", 0),
                            usage.optInt("prompt_cache_miss_tokens", 0));
                }
                JSONArray choices = o.optJSONArray("choices");
                if (choices == null || choices.length() == 0) {
                    continue;
                }
                JSONObject delta = choices.getJSONObject(0).optJSONObject("delta");
                if (delta == null) {
                    continue;
                }
                String piece = delta.optString("content", "");
                if (!piece.isEmpty() && !"null".equals(piece)) {
                    listener.onDelta(piece);
                }
                // 原生思考：DeepSeek 的 reasoner 用 reasoning_content，别家有的叫 reasoning
                String think = delta.optString("reasoning_content", "");
                if (think.isEmpty() || "null".equals(think)) {
                    think = delta.optString("reasoning", "");
                }
                if (!think.isEmpty() && !"null".equals(think)) {
                    listener.onReasoning(think);
                }
                JSONArray tcs = delta.optJSONArray("tool_calls");
                if (tcs != null) {
                    for (int i = 0; i < tcs.length(); i++) {
                        JSONObject t = tcs.getJSONObject(i);
                        int idx = t.optInt("index", 0);
                        ToolCall tc = pending.get(idx);
                        if (tc == null) {
                            tc = new ToolCall();
                            pending.put(idx, tc);
                            order.add(idx);
                        }
                        String id = t.optString("id", "");
                        if (!id.isEmpty() && !"null".equals(id)) {
                            tc.id = id;
                        }
                        JSONObject fn = t.optJSONObject("function");
                        if (fn != null) {
                            String n = fn.optString("name", "");
                            if (!n.isEmpty() && !"null".equals(n)) {
                                tc.name = n;
                            }
                            String a = fn.optString("arguments", "");
                            if (!a.isEmpty() && !"null".equals(a)) {
                                tc.args = tc.args + a;
                            }
                        }
                    }
                }
            }
            br.close();

            List<ToolCall> wanted = new ArrayList<ToolCall>();
            for (int i = 0; i < order.size(); i++) {
                ToolCall tc = pending.get(order.get(i));
                if (tc != null && !tc.name.isEmpty()) {
                    wanted.add(tc);
                }
            }
            // 超时那条路已经报过错了，这里不要再报一次
            if (timeoutMsg[0] != null) {
                return;
            }
            for (int i = 0; i < wanted.size(); i++) {
                ToolCall tc = wanted.get(i);
                listener.onToolCall(tc.id, tc.name, tc.args);
            }
            if (delivered.compareAndSet(false, true)) {
                listener.onDone(null);
            }
        } catch (Exception e) {
            if (timeoutMsg[0] != null) {
                return;
            }
            String m = e.getMessage();
            if (delivered.compareAndSet(false, true)) {
                listener.onDone(e.getClass().getSimpleName() + ": " + (m == null ? "" : m));
            }
        } finally {
            if (watchdog != null) {
                watchdog.interrupt();
            }
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
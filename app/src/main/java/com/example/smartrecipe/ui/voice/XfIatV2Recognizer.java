package com.example.smartrecipe.ui.voice;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.example.smartrecipe.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 讯飞 IAT v2 WebSocket（流式语音听写）
 * 控制台接口：wss://iat-api.xfyun.cn/v2/iat
 *
 * 音频格式要求：
 * - PCM 16kHz
 * - 单声道
 * - 16bit
 * - raw
 */
public class XfIatV2Recognizer {

    public interface Callback {
        /** 增量识别（会多次回调） */
        void onPartialResult(String text);

        /** 最终识别（服务端 status=2 时回调一次） */
        void onFinalResult(String finalText);

        void onError(String message);
    }

    private static final String HOST = "iat-api.xfyun.cn";
    private static final String PATH = "/v2/iat";
    private static final String WS_URL = "wss://" + HOST + PATH;

    // 业务参数（你可以按需调整）
    private static final String LANGUAGE = "zh_cn";
    private static final String DOMAIN = "iat";
    private static final String ACCENT = "mandarin";
    private static final int VAD_EOS = 2000;

    private final OkHttpClient client = new OkHttpClient();

    private WebSocket webSocket;
    private Callback callback;
    private final StringBuilder cumulativeText = new StringBuilder();
    private volatile boolean closed = false;

    /** 开始一次识别会话（建立 WebSocket） */
    public void start(@NonNull Callback cb) {
        this.callback = cb;

        String appId = safeTrim(BuildConfig.IFLYTEK_APP_ID);
        String apiKey = safeTrim(BuildConfig.IFLYTEK_API_KEY);
        String apiSecret = safeTrim(BuildConfig.IFLYTEK_API_SECRET);

        if (appId.isEmpty()) {
            cb.onError("未配置讯飞 APPID（BuildConfig.IFLYTEK_APP_ID 为空）。请检查 local.properties / Rebuild");
            return;
        }
        if (apiKey.isEmpty() || apiSecret.isEmpty()) {
            cb.onError("未配置讯飞 APIKey/APISecret（BuildConfig 为空）。请检查 local.properties / Rebuild");
            return;
        }

        try {
            // RFC1123 GMT 时间
            String date = rfc1123DateGMT(new Date());
            String url = buildAuthUrl(apiKey, apiSecret, date);

            closed = false;
            cumulativeText.setLength(0);

            Request request = new Request.Builder().url(url).build();
            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                    // 连接成功。外部开始 sendAudioFrame(status=0/1/2)
                }

                @Override
                public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                    handleServerMessage(text);
                }

                @Override
                public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, Response response) {
                    if (closed) return;
                    closed = true;
                    if (callback != null) callback.onError("WebSocket 连接失败: " + t.getMessage());
                }

                @Override
                public void onClosing(@NonNull WebSocket ws, int code, @NonNull String reason) {
                    ws.close(code, reason);
                }

                @Override
                public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                    // closed
                }
            });

        } catch (Exception e) {
            cb.onError("启动讯飞 v2 失败: " + e.getMessage());
        }
    }

    /**
     * 发送音频帧
     * status: 0=第一帧, 1=中间帧, 2=最后一帧
     */
    public void sendAudioFrame(@NonNull byte[] pcm, int status) {
        if (webSocket == null || callback == null || closed) return;

        try {
            String appId = safeTrim(BuildConfig.IFLYTEK_APP_ID);

            JSONObject frame = new JSONObject();

            // common
            JSONObject common = new JSONObject();
            common.put("app_id", appId);
            frame.put("common", common);

            // business（一般只在第一帧必须；重复发也能用但浪费）
            JSONObject business = new JSONObject();
            business.put("language", LANGUAGE);
            business.put("domain", DOMAIN);
            business.put("accent", ACCENT);
            business.put("vad_eos", VAD_EOS);
            frame.put("business", business);

            // data
            JSONObject data = new JSONObject();
            data.put("status", status);
            data.put("format", "audio/L16;rate=16000");
            data.put("encoding", "raw");
            data.put("audio", Base64.encodeToString(pcm, Base64.NO_WRAP));
            frame.put("data", data);

            webSocket.send(frame.toString());

        } catch (Exception e) {
            if (callback != null) callback.onError("发送音频失败: " + e.getMessage());
        }
    }

    /** 关闭会话 */
    public void close() {
        closed = true;
        if (webSocket != null) {
            webSocket.close(1000, "client close");
            webSocket = null;
        }
    }

    // -------------------- 解析服务端返回 --------------------

    private void handleServerMessage(String text) {
        try {
            JSONObject obj = new JSONObject(text);
            int code = obj.optInt("code", -1);
            String message = obj.optString("message", "");

            if (code != 0) {
                closed = true;
                if (callback != null) callback.onError("讯飞返回错误: " + message + " (code=" + code + ")");
                close();
                return;
            }

            JSONObject data = obj.optJSONObject("data");
            if (data == null) return;

            int status = data.optInt("status", -1);

            JSONObject result = data.optJSONObject("result");
            if (result != null) {
                String piece = parseWsCw(result);
                if (!piece.isEmpty()) {
                    cumulativeText.append(piece);
                    if (callback != null) callback.onPartialResult(cumulativeText.toString());
                }
            }

            // status=2：服务端判定结束
            if (status == 2) {
                closed = true;
                if (callback != null) callback.onFinalResult(cumulativeText.toString().trim());
                close();
            }

        } catch (Exception e) {
            if (callback != null) callback.onError("解析讯飞返回失败: " + e.getMessage());
        }
    }

    private String parseWsCw(JSONObject result) {
        JSONArray ws = result.optJSONArray("ws");
        if (ws == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ws.length(); i++) {
            JSONObject wsi = ws.optJSONObject(i);
            if (wsi == null) continue;
            JSONArray cw = wsi.optJSONArray("cw");
            if (cw == null || cw.length() == 0) continue;
            JSONObject first = cw.optJSONObject(0);
            if (first != null) sb.append(first.optString("w", ""));
        }
        return sb.toString();
    }

    // -------------------- v2 鉴权：Authorization(HMAC-SHA256) --------------------

    private String buildAuthUrl(String apiKey, String apiSecret, String date) throws Exception {
        // 生成签名原文
        String signatureOrigin =
                "host: " + HOST + "\n" +
                        "date: " + date + "\n" +
                        "GET " + PATH + " HTTP/1.1";

        // HMAC-SHA256 -> base64
        String signatureSha = hmacSha256Base64(apiSecret, signatureOrigin);

        // authorization 原文
        String authorizationOrigin = String.format(
                Locale.US,
                "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                apiKey,
                signatureSha
        );

        // authorization base64
        String authorization = Base64.encodeToString(
                authorizationOrigin.getBytes(StandardCharsets.UTF_8),
                Base64.NO_WRAP
        );

        // 拼接 URL query
        return WS_URL
                + "?authorization=" + urlEncode(authorization)
                + "&date=" + urlEncode(date)
                + "&host=" + urlEncode(HOST);
    }

    private static String hmacSha256Base64(String secret, String data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(raw, Base64.NO_WRAP);
    }

    private static String rfc1123DateGMT(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return fmt.format(date);
    }

    private static String urlEncode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
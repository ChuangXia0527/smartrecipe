package com.example.smartrecipe.ui.voice;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.example.smartrecipe.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 基于讯飞 WebAPI(IAT) 的音频转文字实现。
 * 为了便于接入现有页面，当前实现使用「一次录音 -> 一次请求」模式。
 */
public class XfIatRecognizer {

    public interface Callback {
        void onSuccess(String text);

        void onError(String message);
    }

    private static final String IAT_URL = "https://iat-api.xfyun.cn/v2/iat";

    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void recognize(@NonNull byte[] pcm16k, @NonNull Callback callback) {
        if (BuildConfig.IFLYTEK_APP_ID == null || BuildConfig.IFLYTEK_APP_ID.trim().isEmpty()) {
            callback.onError("未配置讯飞 appId，请在 local.properties 中添加 iflytek.appId");
            return;
        }
        if (BuildConfig.IFLYTEK_API_KEY == null || BuildConfig.IFLYTEK_API_KEY.trim().isEmpty()) {
            callback.onError("未配置讯飞 apiKey，请在 local.properties 中添加 iflytek.apiKey");
            return;
        }
        if (BuildConfig.IFLYTEK_API_SECRET == null || BuildConfig.IFLYTEK_API_SECRET.trim().isEmpty()) {
            callback.onError("未配置讯飞 apiSecret，请在 local.properties 中添加 iflytek.apiSecret");
            return;
        }

        executor.execute(() -> doRecognize(pcm16k, callback));
    }

    private void doRecognize(byte[] pcm16k, Callback callback) {
        try {
            String curTime = String.valueOf(System.currentTimeMillis() / 1000);

            JSONObject param = new JSONObject();
            JSONObject iat = new JSONObject();
            iat.put("language", "zh_cn");
            iat.put("domain", "iat");
            iat.put("accent", "mandarin");
            param.put("iat", iat);

            String paramBase64 = Base64.encodeToString(param.toString().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            String checkSum = md5(BuildConfig.IFLYTEK_API_KEY + curTime + paramBase64);

            RequestBody body = RequestBody.create(
                    Base64.encodeToString(pcm16k, Base64.NO_WRAP),
                    MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(IAT_URL)
                    .addHeader("X-Appid", BuildConfig.IFLYTEK_APP_ID)
                    .addHeader("X-CurTime", curTime)
                    .addHeader("X-Param", paramBase64)
                    .addHeader("X-CheckSum", checkSum)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("讯飞请求失败: HTTP " + response.code());
                    return;
                }
                String resp = response.body().string();
                JSONObject obj = new JSONObject(resp);
                String code = obj.optString("code");
                if (!"0".equals(code)) {
                    callback.onError("讯飞识别失败: " + obj.optString("desc", "未知错误") + " (code=" + code + ")");
                    return;
                }
                String data = obj.optString("data", "");
                if (data.isEmpty()) {
                    callback.onError("讯飞返回空识别结果");
                    return;
                }
                callback.onSuccess(parseResult(data));
            }
        } catch (Exception e) {
            callback.onError("讯飞识别异常: " + e.getMessage());
        }
    }

    private String parseResult(String data) {
        try {
            JSONObject dataObj = new JSONObject(data);
            JSONArray ws = dataObj.optJSONArray("ws");
            if (ws == null) return "";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ws.length(); i++) {
                JSONObject wsi = ws.optJSONObject(i);
                if (wsi == null) continue;
                JSONArray cw = wsi.optJSONArray("cw");
                if (cw == null || cw.length() == 0) continue;
                JSONObject first = cw.optJSONObject(0);
                if (first != null) {
                    sb.append(first.optString("w", ""));
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String md5(String source) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(source.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }
}

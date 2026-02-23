package com.example.smartrecipe.ui.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.ai.nlp.VoiceIntent;
import com.example.smartrecipe.ai.nlp.VoiceIntentParser;
import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.recommend.RecommendEngine;
import com.example.smartrecipe.ui.detail.RecipeDetailActivity;
import com.example.smartrecipe.ui.main.RecipeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoiceActivity extends AppCompatActivity {

    private static final String TAG_VOICE_TRACK = "VoiceTrack";

    private static final int SAMPLE_RATE = 16000;
    private static final int RECORD_SECONDS = 5;

    private TextView tvResultText, tvParsed, tvNoVoiceResult;
    private RecyclerView rvRecommend;
    private RecipeAdapter adapter;
    private final List<Recipe> showList = new ArrayList<>();

    private EditText etText;
    private Button btnStartVoice;

    // ✅ v2 WebSocket 识别器
    private final XfIatV2Recognizer xfIatV2Recognizer = new XfIatV2Recognizer();

    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isRecognizing = new AtomicBoolean(false);

    // 防止重复触发推荐/收尾
    private final AtomicBoolean finishedOnce = new AtomicBoolean(false);

    // 申请录音权限
    private final ActivityResultLauncher<String> requestAudioPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startVoiceInput();
                } else {
                    trackVoiceEvent("permission_denied", "request_permission_result_denied");
                    showPermissionDeniedDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        btnStartVoice = findViewById(R.id.btnStartVoice);
        Button btnTextRecommend = findViewById(R.id.btnTextRecommend);
        etText = findViewById(R.id.etText);

        tvResultText = findViewById(R.id.tvResultText);
        tvParsed = findViewById(R.id.tvParsed);
        tvNoVoiceResult = findViewById(R.id.tvNoVoiceResult);
        rvRecommend = findViewById(R.id.rvRecommend);

        rvRecommend.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(showList, recipe -> {
            Intent it = new Intent(VoiceActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        });
        rvRecommend.setAdapter(adapter);

        // 语音按钮：先检查权限
        btnStartVoice.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        // 文本推荐按钮：兜底方案
        btnTextRecommend.setOnClickListener(v -> {
            String text = etText.getText() == null ? "" : etText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入一句需求，例如：低脂鸡胸肉，不要辣", Toast.LENGTH_SHORT).show();
                return;
            }
            runRecommendWithText(text);
        });
    }

    private void startVoiceInput() {
        if (!isRecognizing.compareAndSet(false, true)) {
            Toast.makeText(this, "正在识别中，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }

        finishedOnce.set(false);
        btnStartVoice.setEnabled(false);
        btnStartVoice.setText("录音中...");
        Toast.makeText(this, "开始录音（5秒）...", Toast.LENGTH_SHORT).show();

        // 1) 建立 WebSocket（v2）
        xfIatV2Recognizer.start(new XfIatV2Recognizer.Callback() {
            @Override
            public void onPartialResult(String text) {
                runOnUiThread(() -> tvResultText.setText("识别结果：" + text));
            }

            @Override
            public void onFinalResult(String finalText) {
                if (!finishedOnce.compareAndSet(false, true)) return;

                runOnUiThread(() -> {
                    finishRecognizingUi();

                    String text = finalText == null ? "" : finalText.trim();
                    if (text.isEmpty()) {
                        trackVoiceEvent("empty_result", "iflytek_final_empty");
                        Toast.makeText(VoiceActivity.this, "识别结果为空，请改用文本输入", Toast.LENGTH_SHORT).show();
                        fallbackToTextInput();
                        return;
                    }
                    runRecommendWithText(text);
                });
            }

            @Override
            public void onError(String message) {
                if (!finishedOnce.compareAndSet(false, true)) return;

                runOnUiThread(() -> {
                    finishRecognizingUi();
                    trackVoiceEvent("recognition_failure", message);
                    Toast.makeText(VoiceActivity.this, message, Toast.LENGTH_LONG).show();
                    fallbackToTextInput();
                });
            }
        });

        // 2) 录音并分片发送（status 0/1/2）
        audioExecutor.execute(() -> {
            boolean ok = recordAndStreamToXfV2(RECORD_SECONDS);

            if (!ok) {
                if (!finishedOnce.compareAndSet(false, true)) return;
                runOnUiThread(() -> {
                    finishRecognizingUi();
                    trackVoiceEvent("recognition_failure", "record_stream_failed");
                    Toast.makeText(this, "录音/发送失败，请改用文本输入", Toast.LENGTH_SHORT).show();
                    fallbackToTextInput();
                });
            }
        });
    }

    /**
     * 录音并流式发送到讯飞 v2。
     * 录满 seconds 秒后发送 status=2 结束帧。
     */
    private boolean recordAndStreamToXfV2(int seconds) {
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, channelConfig, audioFormat);
        if (minBuffer == AudioRecord.ERROR || minBuffer == AudioRecord.ERROR_BAD_VALUE) {
            return false;
        }

        // 40ms 每帧：16000Hz * 2bytes * 0.04 = 1280 bytes
        int frameSize = 1280;
        int bufferSize = Math.max(minBuffer, frameSize * 4);

        AudioRecord audioRecord;
        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    channelConfig,
                    audioFormat,
                    bufferSize
            );
        } catch (SecurityException e) {
            return false;
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release();
            return false;
        }

        byte[] frameBuf = new byte[frameSize];

        try {
            audioRecord.startRecording();

            long endAt = System.currentTimeMillis() + seconds * 1000L;
            boolean first = true;

            while (System.currentTimeMillis() < endAt) {
                int read = audioRecord.read(frameBuf, 0, frameBuf.length);
                if (read > 0) {
                    byte[] frame = new byte[read];
                    System.arraycopy(frameBuf, 0, frame, 0, read);

                    if (first) {
                        xfIatV2Recognizer.sendAudioFrame(frame, 0); // 第一帧
                        first = false;
                    } else {
                        xfIatV2Recognizer.sendAudioFrame(frame, 1); // 中间帧
                    }
                }
            }

            // 最后一帧：结束
            xfIatV2Recognizer.sendAudioFrame(new byte[0], 2);
            return true;

        } catch (Exception e) {
            return false;
        } finally {
            try {
                audioRecord.stop();
            } catch (Exception ignore) {}
            audioRecord.release();
        }
    }

    private void finishRecognizingUi() {
        isRecognizing.set(false);
        btnStartVoice.setEnabled(true);
        btnStartVoice.setText("开始语音输入");
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要录音权限")
                .setMessage("语音输入需要录音权限，请前往系统设置开启后重试。")
                .setNegativeButton("取消", null)
                .setPositiveButton("去设置开启权限", (dialog, which) -> openAppSettings())
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void fallbackToTextInput() {
        etText.requestFocus();
    }

    private void trackVoiceEvent(String event, String detail) {
        Log.i(TAG_VOICE_TRACK, "event=" + event + ", detail=" + detail);
    }

    // ========================= 关键：硬过滤（强约束） =========================

    /** 把 List<String> 拼成一个可 contains 的字符串（用空格连接） */
    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString();
    }

    /** 把 recipe 的 name + ingredients + tags 合成一个可检索文本 */
    private String recipeToSearchText(Recipe r) {
        String name = r.getName() == null ? "" : r.getName();
        String ingredientsText = joinList(r.getIngredients());
        String tagsText = joinList(r.getTags());
        return (name + " " + ingredientsText + " " + tagsText).toLowerCase(Locale.ROOT);
    }

    /**
     * 强制过滤策略：
     * - 避免食材 avoidIngredients：命中任意一个就剔除
     * - 想要食材 needIngredients：如果非空，则至少命中一个才保留
     *
     * 匹配位置：菜名/食材/标签（合并文本 contains）
     */
    private List<Recipe> hardFilterByIntent(List<Recipe> all, VoiceIntent intent) {
        List<Recipe> out = new ArrayList<>();

        for (Recipe r : all) {
            String haystack = recipeToSearchText(r);

            // 1) 避免：命中就剔除
            boolean hitAvoid = false;
            if (intent.avoidIngredients != null) {
                for (String a : intent.avoidIngredients) {
                    if (a == null) continue;
                    String key = a.trim().toLowerCase(Locale.ROOT);
                    if (!key.isEmpty() && haystack.contains(key)) {
                        hitAvoid = true;
                        break;
                    }
                }
            }
            if (hitAvoid) continue;

            // 2) 想要：至少命中一个才保留
            if (intent.needIngredients != null && !intent.needIngredients.isEmpty()) {
                boolean hitNeed = false;
                for (String w : intent.needIngredients) {
                    if (w == null) continue;
                    String key = w.trim().toLowerCase(Locale.ROOT);
                    if (!key.isEmpty() && haystack.contains(key)) {
                        hitNeed = true;
                        break;
                    }
                }
                if (!hitNeed) continue;
            }

            out.add(r);
        }

        return out;
    }

    /**
     * 无论语音还是文本，统一走这个推荐流程
     */
    private void runRecommendWithText(String text) {
        tvResultText.setText("识别结果：" + text);

        VoiceIntent intent = VoiceIntentParser.parse(text);
        tvParsed.setText("解析结果：\n" + intent.toReadable());

        List<Recipe> all = RecipeRepository.getAllRecipes(this);

        // ✅ 先硬过滤（想要/不要）再排序推荐
        List<Recipe> filtered = hardFilterByIntent(all, intent);
        List<Recipe> rec = RecommendEngine.recommendByVoiceIntent(filtered, intent, 10);

        showList.clear();
        showList.addAll(rec);
        adapter.notifyDataSetChanged();

        boolean empty = rec.isEmpty();
        tvNoVoiceResult.setVisibility(empty ? TextView.VISIBLE : TextView.GONE);
        if (empty) {
            Toast.makeText(this, "没有匹配到推荐结果（试试换个说法）", Toast.LENGTH_SHORT).show();
        }
    }
}
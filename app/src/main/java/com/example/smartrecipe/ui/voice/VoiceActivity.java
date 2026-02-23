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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
    private final XfIatRecognizer xfIatRecognizer = new XfIatRecognizer();
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isRecognizing = new AtomicBoolean(false);

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

        // 文本推荐按钮：兜底方案，保证演示稳定
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

        btnStartVoice.setEnabled(false);
        btnStartVoice.setText("录音中...");
        Toast.makeText(this, "开始录音（5秒）...", Toast.LENGTH_SHORT).show();

        audioExecutor.execute(() -> {
            byte[] pcmData = recordPcmData();
            if (pcmData == null || pcmData.length == 0) {
                runOnUiThread(() -> {
                    trackVoiceEvent("recognition_failure", "record_empty_data");
                    Toast.makeText(this, "录音失败，请改用文本输入", Toast.LENGTH_SHORT).show();
                    fallbackToTextInput();
                    finishRecognizingUi();
                });
                return;
            }

            xfIatRecognizer.recognize(pcmData, new XfIatRecognizer.Callback() {
                @Override
                public void onSuccess(String text) {
                    runOnUiThread(() -> {
                        finishRecognizingUi();
                        if (text == null || text.trim().isEmpty()) {
                            trackVoiceEvent("empty_result", "iflytek_result_empty");
                            Toast.makeText(VoiceActivity.this, "识别结果为空，请改用文本输入", Toast.LENGTH_SHORT).show();
                            fallbackToTextInput();
                            return;
                        }
                        runRecommendWithText(text.trim());
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        finishRecognizingUi();
                        trackVoiceEvent("recognition_failure", message);
                        Toast.makeText(VoiceActivity.this, message, Toast.LENGTH_LONG).show();
                        fallbackToTextInput();
                    });
                }
            });
        });
    }

    private byte[] recordPcmData() {
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        int minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, channelConfig, audioFormat);
        if (minBuffer == AudioRecord.ERROR || minBuffer == AudioRecord.ERROR_BAD_VALUE) {
            return null;
        }

        int bufferSize = Math.max(minBuffer, SAMPLE_RATE);
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
            return null;
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release();
            return null;
        }

        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            audioRecord.startRecording();
            long endAt = System.currentTimeMillis() + RECORD_SECONDS * 1000L;
            while (System.currentTimeMillis() < endAt) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                audioRecord.stop();
            } catch (Exception ignore) {
                // ignore
            }
            audioRecord.release();
        }
        return output.toByteArray();
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

    /**
     * 无论语音还是文本，统一走这个推荐流程
     */
    private void runRecommendWithText(String text) {
        tvResultText.setText("识别结果：" + text);

        VoiceIntent intent = VoiceIntentParser.parse(text);
        tvParsed.setText("解析结果：\n" + intent.toReadable());

        List<Recipe> all = RecipeRepository.getAllRecipes(this);
        List<Recipe> rec = RecommendEngine.recommendByVoiceIntent(all, intent, 10);

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

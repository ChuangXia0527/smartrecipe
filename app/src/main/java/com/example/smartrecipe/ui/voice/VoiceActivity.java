package com.example.smartrecipe.ui.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class VoiceActivity extends AppCompatActivity {

    private static final String TAG_VOICE_TRACK = "VoiceTrack";

    private TextView tvResultText, tvParsed;
    private RecyclerView rvRecommend;
    private RecipeAdapter adapter;
    private final List<Recipe> showList = new ArrayList<>();

    private EditText etText;

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

    // 启动系统语音识别面板并接收结果
    private final ActivityResultLauncher<Intent> voiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    trackVoiceEvent("recognition_failure", "result_not_ok_or_data_null");
                    Toast.makeText(this, "未识别到语音，请改用文本输入", Toast.LENGTH_SHORT).show();
                    fallbackToTextInput();
                    return;
                }
                ArrayList<String> list = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (list == null || list.isEmpty()) {
                    trackVoiceEvent("empty_result", "extra_results_empty");
                    Toast.makeText(this, "识别结果为空，请改用文本输入", Toast.LENGTH_SHORT).show();
                    fallbackToTextInput();
                    return;
                }
                String text = list.get(0);
                // 统一走同一套推荐流程
                runRecommendWithText(text);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        Button btnStartVoice = findViewById(R.id.btnStartVoice);
        Button btnTextRecommend = findViewById(R.id.btnTextRecommend);
        etText = findViewById(R.id.etText);

        tvResultText = findViewById(R.id.tvResultText);
        tvParsed = findViewById(R.id.tvParsed);
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
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // 强制中文，提高识别成功率
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "zh-CN");

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出你的需求，例如：低脂鸡胸肉，不要辣");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        if (intent.resolveActivity(getPackageManager()) == null) {
            trackVoiceEvent("recognition_failure", "recognizer_intent_unavailable");
            Toast.makeText(this, "当前设备不支持语音识别，已切换为文本输入", Toast.LENGTH_SHORT).show();
            fallbackToTextInput();
            return;
        }

        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            trackVoiceEvent("recognition_failure", "launch_exception:" + e.getClass().getSimpleName());
            Toast.makeText(this, "语音识别启动失败，已切换为文本输入", Toast.LENGTH_SHORT).show();
            fallbackToTextInput();
        }
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

        if (rec.isEmpty()) {
            Toast.makeText(this, "没有匹配到推荐结果（试试换个说法）", Toast.LENGTH_SHORT).show();
        }
    }
}

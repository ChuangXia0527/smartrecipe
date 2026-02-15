package com.example.smartrecipe.ui.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class VoiceActivity extends AppCompatActivity {

    private TextView tvResultText, tvParsed;
    private RecyclerView rvRecommend;
    private RecipeAdapter adapter;
    private final List<Recipe> showList = new ArrayList<>();

    private EditText etText;

    // 申请录音权限
    private final ActivityResultLauncher<String> requestAudioPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startVoiceInput();
                else Toast.makeText(this, "没有录音权限，无法语音识别", Toast.LENGTH_SHORT).show();
            });

    // 启动系统语音识别面板并接收结果
    private final ActivityResultLauncher<Intent> voiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    Toast.makeText(this, "未识别到语音", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<String> list = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (list == null || list.isEmpty()) {
                    Toast.makeText(this, "识别结果为空", Toast.LENGTH_SHORT).show();
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

        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "当前设备不支持语音识别", Toast.LENGTH_SHORT).show();
        }
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

package com.example.smartrecipe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.AiConfig;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.ui.auth.AuthActivity;

public class AdminAiConfigActivity extends AppCompatActivity {

    private EditText etSimilarity, etPreferenceWeight, etNutritionBase, etTabooBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_ai_config);
        etSimilarity = findViewById(R.id.etSimilarity);
        etPreferenceWeight = findViewById(R.id.etPreferenceWeight);
        etNutritionBase = findViewById(R.id.etNutritionBase);
        etTabooBase = findViewById(R.id.etTabooBase);
        findViewById(R.id.btnSaveAiConfig).setOnClickListener(v -> save());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AiConfig config = AdminRepository.getAiConfig(this);
        etSimilarity.setText(String.valueOf(config.similarityThreshold));
        etPreferenceWeight.setText(String.valueOf(config.preferenceWeight));
        etNutritionBase.setText(config.nutritionBase);
        etTabooBase.setText(config.tabooBase);
    }

    private void save() {
        double sim = parse(etSimilarity.getText().toString(), 0.6);
        double weight = parse(etPreferenceWeight.getText().toString(), 0.4);
        AdminRepository.saveAiConfig(this, sim, weight, etNutritionBase.getText().toString().trim(), etTabooBase.getText().toString().trim());
        Toast.makeText(this, "AI配置已保存", Toast.LENGTH_SHORT).show();
    }

    private double parse(String v, double d) {
        try { return Double.parseDouble(v); } catch (Exception ignored) { return d; }
    }
}

package com.example.smartrecipe.ui.detail;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvMeta, tvTags, tvIngredients, tvSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvTags = findViewById(R.id.tvTags);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvSteps = findViewById(R.id.tvSteps);

        int id = getIntent().getIntExtra("recipe_id", -1);
        Recipe r = RecipeRepository.findById(this, id);

        if (r == null) {
            tvTitle.setText("未找到食谱");
            return;
        }

        tvTitle.setText(r.getName());
        tvMeta.setText(r.getMinutes() + "分钟 · " + r.getCalorie() + "kcal");
        tvTags.setText("标签：" + joinWithSlash(r.getTags()));
        tvIngredients.setText(joinWithComma(r.getIngredients()));
        tvSteps.setText(formatSteps(r.getSteps()));
    }

    private String joinWithSlash(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) sb.append(" / ");
        }
        return sb.toString();
    }

    private String joinWithComma(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) sb.append("、");
        }
        return sb.toString();
    }

    private String formatSteps(List<String> steps) {
        if (steps == null || steps.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            sb.append(i + 1).append(". ").append(steps.get(i));
            if (i != steps.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }
}

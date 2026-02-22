package com.example.smartrecipe.ui.detail;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.ui.common.RecipeImageResolver;
import com.example.smartrecipe.data.user.UserRepository;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvMeta, tvTags, tvIngredients, tvSteps, tvRecipeCoverEmoji;
    private ImageView ivRecipeCover;
    private Button btnFavorite;
    private Recipe recipe;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvTags = findViewById(R.id.tvTags);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvSteps = findViewById(R.id.tvSteps);
        ivRecipeCover = findViewById(R.id.ivRecipeCover);
        tvRecipeCoverEmoji = findViewById(R.id.tvRecipeCoverEmoji);
        btnFavorite = findViewById(R.id.btnFavoriteToggle);

        userId = SessionManager.currentUserId(this);

        int id = getIntent().getIntExtra("recipe_id", -1);
        recipe = RecipeRepository.findById(this, id);

        if (recipe == null) {
            tvTitle.setText("未找到食谱");
            return;
        }

        UserRepository.trackRecipeOpen(this, userId, recipe.getId());

        tvTitle.setText(recipe.getName());
        tvMeta.setText(recipe.getMinutes() + "分钟 · " + recipe.getCalorie() + "kcal");
        tvTags.setText("标签：" + joinWithSlash(recipe.getTags()));
        tvIngredients.setText(joinWithComma(recipe.getIngredients()));
        tvSteps.setText(formatSteps(recipe.getSteps()));
        ivRecipeCover.setBackgroundResource(RecipeImageResolver.resolveBackgroundRes(recipe));
        tvRecipeCoverEmoji.setText(RecipeImageResolver.resolveEmoji(recipe));

        refreshFavoriteState();
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void toggleFavorite() {
        if (userId <= 0 || recipe == null) return;
        boolean favorited = UserRepository.isFavorite(this, userId, recipe.getId());
        if (favorited) {
            UserRepository.removeFavorite(this, userId, recipe.getId());
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
        } else {
            UserRepository.addFavorite(this, userId, recipe.getId());
            Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
        }
        refreshFavoriteState();
    }

    private void refreshFavoriteState() {
        if (recipe == null || userId <= 0) return;
        boolean favorited = UserRepository.isFavorite(this, userId, recipe.getId());
        btnFavorite.setText(favorited ? "取消收藏" : "收藏食谱");
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

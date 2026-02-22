package com.example.smartrecipe.ui.recognize;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.ui.detail.RecipeDetailActivity;
import com.example.smartrecipe.ui.main.RecipeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecognizeResultActivity extends AppCompatActivity {

    private IngredientCheckAdapter ingAdapter;
    private RecipeAdapter recipeAdapter;
    private final List<Recipe> showRecipes = new ArrayList<>();
    private TextView tvSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_result);

        RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        RecyclerView rvRecommend = findViewById(R.id.rvRecommend);
        Button btnGenerate = findViewById(R.id.btnGenerate);
        tvSelected = findViewById(R.id.tvSelected);

        ArrayList<String> ingredients = getIntent().getStringArrayListExtra("ingredients");
        if (ingredients == null) ingredients = new ArrayList<>();

        ArrayList<String> mutableIngredients = new ArrayList<>(ingredients);

        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingAdapter = new IngredientCheckAdapter(mutableIngredients);
        rvIngredients.setAdapter(ingAdapter);

        rvRecommend.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(showRecipes, recipe -> {
            Intent it = new Intent(RecognizeResultActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        });
        rvRecommend.setAdapter(recipeAdapter);

        btnGenerate.setOnClickListener(v -> generateRecommend());
    }

    private void generateRecommend() {
        Set<String> selected = ingAdapter.getSelected();
        if (selected == null || selected.isEmpty()) {
            Toast.makeText(this, "请至少选择一种食材", Toast.LENGTH_SHORT).show();
            return;
        }

        tvSelected.setText("已选择：" + String.join("、", selected));

        List<Recipe> allRecipes = RecipeRepository.getAllRecipes(this);
        List<RankedRecipe> matchedRecipes = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) continue;
            int hit = 0;
            for (String ingredient : selected) {
                if (recipe.getIngredients().contains(ingredient)) hit++;
            }
            if (hit > 0) {
                int score = hit * 10 + Math.max(0, 30 - recipe.getMinutes());
                matchedRecipes.add(new RankedRecipe(recipe, score));
            }
        }

        matchedRecipes.sort((a, b) -> Integer.compare(b.score, a.score));

        showRecipes.clear();
        for (RankedRecipe rankedRecipe : matchedRecipes) {
            showRecipes.add(rankedRecipe.recipe);
        }
        recipeAdapter.notifyDataSetChanged();

        if (showRecipes.isEmpty()) {
            Toast.makeText(this, "没有匹配到推荐结果，请尝试更换食材组合", Toast.LENGTH_SHORT).show();
        }
    }

    private static class RankedRecipe {
        Recipe recipe;
        int score;

        RankedRecipe(Recipe recipe, int score) {
            this.recipe = recipe;
            this.score = score;
        }
    }
}

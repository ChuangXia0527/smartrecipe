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

        // 保证列表是可变的
        ArrayList<String> mutableIngredients = new ArrayList<>(ingredients);

        // 1) 勾选食材列表
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingAdapter = new IngredientCheckAdapter(mutableIngredients);
        rvIngredients.setAdapter(ingAdapter);

        // 2) 推荐列表
        rvRecommend.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(showRecipes, recipe -> {
            Intent it = new Intent(RecognizeResultActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId()); // 确保 Recipe 类有 id
            startActivity(it);
        });
        rvRecommend.setAdapter(recipeAdapter);

        // 3) 生成推荐
        btnGenerate.setOnClickListener(v -> generateRecommend());
    }

    // 生成推荐结果
    private void generateRecommend() {
        // 获取用户选择的食材
        Set<String> selected = ingAdapter.getSelected();
        if (selected == null || selected.isEmpty()) {
            Toast.makeText(this, "请至少选择一种食材", Toast.LENGTH_SHORT).show();
            return;
        }

        tvSelected.setText("已选择：" + String.join("、", selected));

        // 使用 RecipeRepository 来获取所有的食谱
        List<Recipe> allRecipes = RecipeRepository.getAllRecipes(this);
        List<Recipe> matchedRecipes = new ArrayList<>();

        // 遍历所有食谱，查找包含用户选择食材的食谱
        for (Recipe recipe : allRecipes) {
            boolean isMatch = true;
            for (String ingredient : selected) {
                if (!recipe.getIngredients().contains(ingredient)) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) {
                matchedRecipes.add(recipe);
            }
        }

        // 如果没有找到匹配的食谱，给用户提示
        if (matchedRecipes.isEmpty()) {
            Toast.makeText(this, "没有匹配到推荐结果，请尝试添加更多食材", Toast.LENGTH_SHORT).show();
        }

        // 显示匹配的食谱
        showRecipes.clear();
        showRecipes.addAll(matchedRecipes);
        recipeAdapter.notifyDataSetChanged();
    }
}

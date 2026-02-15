package com.example.smartrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.ui.detail.RecipeDetailActivity;
import com.example.smartrecipe.ui.main.RecipeAdapter;
import com.example.smartrecipe.ui.recognize.RecognizeActivity;
import com.example.smartrecipe.ui.voice.VoiceActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnRecognize = findViewById(R.id.btnRecognize);
        Button btnVoice = findViewById(R.id.btnVoice);

        btnRecognize.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));

        btnVoice.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VoiceActivity.class)));

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 获取食谱数据
        List<Recipe> recipes = RecipeRepository.getAllRecipes(this);
        Log.d("MainActivity", "Loaded recipes: " + recipes.size());  // 输出加载的食谱数量

        if (recipes != null && !recipes.isEmpty()) {
            // 如果数据加载成功，设置适配器
            RecipeAdapter recipeAdapter = new RecipeAdapter(recipes, recipe -> {
                Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
                it.putExtra("recipe_id", recipe.getId());
                startActivity(it);
            });
            rv.setAdapter(recipeAdapter);
        } else {
            Log.e("MainActivity", "No recipes loaded or empty data.");
        }
    }
}

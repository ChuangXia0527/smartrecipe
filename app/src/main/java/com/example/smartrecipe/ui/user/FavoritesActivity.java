package com.example.smartrecipe.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.example.smartrecipe.ui.detail.RecipeDetailActivity;
import com.example.smartrecipe.ui.main.RecipeAdapter;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_recipe_list);
        TextView tvTitle = findViewById(R.id.tvPageTitle);
        RecyclerView rv = findViewById(R.id.rvPageRecipes);
        tvTitle.setText("我的收藏");

        long userId = SessionManager.currentUserId(this);
        List<Integer> ids = UserRepository.favoriteRecipeIds(this, userId);
        List<Recipe> recipes = RecipeRepository.findByIds(this, ids);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RecipeAdapter(recipes, recipe -> {
            Intent it = new Intent(this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        }));
    }
}

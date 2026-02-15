package com.example.smartrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.local.entity.UserPreference;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.example.smartrecipe.recommend.PersonalizedRecommendEngine;
import com.example.smartrecipe.ui.auth.AuthActivity;
import com.example.smartrecipe.ui.detail.RecipeDetailActivity;
import com.example.smartrecipe.ui.main.RecipeAdapter;
import com.example.smartrecipe.ui.recognize.RecognizeActivity;
import com.example.smartrecipe.ui.user.FavoritesActivity;
import com.example.smartrecipe.ui.user.HistoryActivity;
import com.example.smartrecipe.ui.user.UserPreferenceActivity;
import com.example.smartrecipe.ui.voice.VoiceActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter recipeAdapter;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            Intent authIntent = new Intent(this, AuthActivity.class);
            if (authIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(authIntent);
                finish();
                return;
            } else {
                Toast.makeText(this, "登录页未注册，已进入游客模式", Toast.LENGTH_LONG).show();
            }
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        userId = SessionManager.currentUserId(this);

        setContentView(R.layout.activity_main);

        Button btnRecognize = findViewById(R.id.btnRecognize);
        Button btnVoice = findViewById(R.id.btnVoice);
        Button btnSearch = findViewById(R.id.btnSearch);
        Button btnPreference = findViewById(R.id.btnPreference);
        Button btnFavorite = findViewById(R.id.btnFavorite);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        EditText etSearch = findViewById(R.id.etSearch);

        btnRecognize.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));

        btnVoice.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VoiceActivity.class)));

        btnPreference.setOnClickListener(v -> startActivity(new Intent(this, UserPreferenceActivity.class)));
        btnFavorite.setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        btnLogout.setOnClickListener(v -> {
            SessionManager.logout(this);
            Intent it = new Intent(this, AuthActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
        });

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Recipe> allRecipes = RecipeRepository.getAllRecipes(this);
        UserPreference pref = UserRepository.getPreference(this, userId);
        List<Recipe> homeRecommend = PersonalizedRecommendEngine.recommend(
                allRecipes,
                pref,
                UserRepository.favoriteRecipeIds(this, userId),
                UserRepository.behaviorRecipeScores(this, userId),
                30
        );

        recipeAdapter = new RecipeAdapter(homeRecommend, recipe -> {
            Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        });
        rv.setAdapter(recipeAdapter);

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            List<Recipe> searchResult = RecipeRepository.search(this, keyword);
            rv.setAdapter(new RecipeAdapter(searchResult, recipe -> {
                Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
                it.putExtra("recipe_id", recipe.getId());
                startActivity(it);
            }));
            if (!keyword.isEmpty()) {
                UserRepository.trackSearch(this, userId, keyword);
            }
            Toast.makeText(this, "共找到 " + searchResult.size() + " 个食谱", Toast.LENGTH_SHORT).show();
        });
    }
}

package com.example.smartrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter recipeAdapter;
    private long userId;

    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> homeRecommend = new ArrayList<>();

    private TextView tvSectionTitle;
    private TextView tvEmpty;

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
        Button btnQuickLowFat = findViewById(R.id.btnQuickLowFat);
        Button btnQuickFast = findViewById(R.id.btnQuickFast);
        Button btnResetList = findViewById(R.id.btnResetList);

        EditText etSearch = findViewById(R.id.etSearch);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEmpty = findViewById(R.id.tvEmpty);

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

        recipeAdapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        });
        rv.setAdapter(recipeAdapter);

        allRecipes = RecipeRepository.getAllRecipes(this);
        UserPreference pref = UserRepository.getPreference(this, userId);
        homeRecommend = PersonalizedRecommendEngine.recommend(
                allRecipes,
                pref,
                UserRepository.favoriteRecipeIds(this, userId),
                UserRepository.behaviorRecipeScores(this, userId),
                30
        );
        renderRecipes(homeRecommend, "为你推荐");

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            List<Recipe> searchResult = RecipeRepository.search(this, keyword);
            renderRecipes(searchResult, keyword.isEmpty() ? "全部食谱" : "搜索结果：" + keyword);
            if (!keyword.isEmpty()) {
                UserRepository.trackSearch(this, userId, keyword);
            }
            Toast.makeText(this, "共找到 " + searchResult.size() + " 个食谱", Toast.LENGTH_SHORT).show();
        });

        btnQuickLowFat.setOnClickListener(v -> {
            List<Recipe> out = new ArrayList<>();
            for (Recipe recipe : allRecipes) {
                if (recipe.getCalorie() <= 400) out.add(recipe);
            }
            renderRecipes(out, "低脂优先（≤400 kcal）");
        });

        btnQuickFast.setOnClickListener(v -> {
            List<Recipe> out = new ArrayList<>();
            for (Recipe recipe : allRecipes) {
                if (recipe.getMinutes() <= 20) out.add(recipe);
            }
            renderRecipes(out, "快手优先（≤20 分钟）");
        });

        btnResetList.setOnClickListener(v -> renderRecipes(homeRecommend, "为你推荐"));
    }

    private void renderRecipes(List<Recipe> list, String sectionTitle) {
        tvSectionTitle.setText(sectionTitle);
        recipeAdapter.replaceData(list);
        tvEmpty.setVisibility(list == null || list.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }
}

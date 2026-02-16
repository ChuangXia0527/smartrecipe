package com.example.smartrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.example.smartrecipe.ui.main.FavoriteGridAdapter;
import com.example.smartrecipe.ui.main.RecipeAdapter;
import com.example.smartrecipe.ui.recognize.RecognizeActivity;
import com.example.smartrecipe.ui.user.FavoritesActivity;
import com.example.smartrecipe.ui.user.HistoryActivity;
import com.example.smartrecipe.ui.user.UserPreferenceActivity;
import com.example.smartrecipe.ui.voice.VoiceActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long userId;

    private TextView tvMainTitle;
    private View panelHome, panelDiscover, panelFavorite, panelMine;
    private TextView tabHome, tabDiscover, tabFavorite, tabMine;

    private RecyclerView rvRecipes;
    private RecyclerView rvFavoriteGrid;
    private EditText etSearch;

    private final List<Recipe> homeRecipes = new ArrayList<>();
    private final List<Recipe> favoriteRecipes = new ArrayList<>();

    private RecipeAdapter recipeAdapter;
    private FavoriteGridAdapter favoriteGridAdapter;
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

        bindViews();
        setupLists();
        setupMineActions();
        setupTabActions();
        loadHomeData();
        loadFavoriteData();
        switchTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteData();
    }

    private void bindViews() {
        tvMainTitle = findViewById(R.id.tvMainTitle);
        panelHome = findViewById(R.id.panelHome);
        panelDiscover = findViewById(R.id.panelDiscover);
        panelFavorite = findViewById(R.id.panelFavorite);
        panelMine = findViewById(R.id.panelMine);

        tabHome = findViewById(R.id.tabHome);
        tabDiscover = findViewById(R.id.tabDiscover);
        tabFavorite = findViewById(R.id.tabFavorite);
        tabMine = findViewById(R.id.tabMine);

        rvRecipes = findViewById(R.id.rvRecipes);
        rvFavoriteGrid = findViewById(R.id.rvFavoriteGrid);
        etSearch = findViewById(R.id.etSearch);

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            List<Recipe> searchResult = RecipeRepository.search(this, keyword);
            homeRecipes.clear();
            homeRecipes.addAll(searchResult);
            recipeAdapter.notifyDataSetChanged();
            if (!keyword.isEmpty()) {
                UserRepository.trackSearch(this, userId, keyword);
            }
            Toast.makeText(this, "共找到 " + searchResult.size() + " 个食谱", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLists() {
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(homeRecipes, recipe -> openDetail(recipe.getId()));
        rvRecipes.setAdapter(recipeAdapter);

        rvFavoriteGrid.setLayoutManager(new GridLayoutManager(this, 3));
        favoriteGridAdapter = new FavoriteGridAdapter(favoriteRecipes, recipe -> openDetail(recipe.getId()));
        rvFavoriteGrid.setAdapter(favoriteGridAdapter);
    }

    private void setupMineActions() {
        Button btnRecognize = findViewById(R.id.btnRecognize);
        Button btnVoice = findViewById(R.id.btnVoice);
        Button btnPreference = findViewById(R.id.btnPreference);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnRecognize.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));
        btnVoice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VoiceActivity.class)));
        btnPreference.setOnClickListener(v -> startActivity(new Intent(this, UserPreferenceActivity.class)));

        btnRecognize.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));
        btnVoice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VoiceActivity.class)));
        btnPreference.setOnClickListener(v -> startActivity(new Intent(this, UserPreferenceActivity.class)));

        btnRecognize.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));
        btnVoice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VoiceActivity.class)));
        btnPreference.setOnClickListener(v -> startActivity(new Intent(this, UserPreferenceActivity.class)));
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
    }

    private void setupTabActions() {
        tabHome.setOnClickListener(v -> switchTab(0));
        tabDiscover.setOnClickListener(v -> switchTab(1));
        tabFavorite.setOnClickListener(v -> switchTab(2));
        tabMine.setOnClickListener(v -> switchTab(3));
    }

    private void loadHomeData() {
        List<Recipe> allRecipes = RecipeRepository.getAllRecipes(this);
        UserPreference pref = UserRepository.getPreference(this, userId);
        List<Recipe> recommends = PersonalizedRecommendEngine.recommend(

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Recipe> allRecipes = RecipeRepository.getAllRecipes(this);
        UserPreference pref = UserRepository.getPreference(this, userId);
        List<Recipe> homeRecommend = PersonalizedRecommendEngine.recommend(
                allRecipes,
                pref,
                UserRepository.favoriteRecipeIds(this, userId),
                UserRepository.behaviorRecipeScores(this, userId),
                50
        );

        homeRecipes.clear();
        homeRecipes.addAll(recommends);
        recipeAdapter.notifyDataSetChanged();
    }

    private void loadFavoriteData() {
        List<Integer> ids = UserRepository.favoriteRecipeIds(this, userId);
        List<Recipe> recipes = RecipeRepository.findByIds(this, ids);
        favoriteRecipes.clear();
        favoriteRecipes.addAll(recipes);
        if (favoriteGridAdapter != null) favoriteGridAdapter.notifyDataSetChanged();
    }

    private void openDetail(int recipeId) {
        Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
        it.putExtra("recipe_id", recipeId);
        startActivity(it);
    }

    private void switchTab(int tabIndex) {
        panelHome.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        panelDiscover.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        panelFavorite.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        panelMine.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        int active = 0xFFF05A22;
        int inactive = 0xFFE7B623;
        tabHome.setTextColor(tabIndex == 0 ? active : inactive);
        tabDiscover.setTextColor(tabIndex == 1 ? active : inactive);
        tabFavorite.setTextColor(tabIndex == 2 ? active : inactive);
        tabMine.setTextColor(tabIndex == 3 ? active : inactive);

        if (tabIndex == 0) tvMainTitle.setText("菜谱大全");
        if (tabIndex == 1) tvMainTitle.setText("发现");
        if (tabIndex == 2) tvMainTitle.setText("我的收藏");
        if (tabIndex == 3) tvMainTitle.setText("我的");
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

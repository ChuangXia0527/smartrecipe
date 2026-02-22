package com.example.smartrecipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.data.entity.Recipe;
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
import com.example.smartrecipe.ui.user.ProfileActivity;
import com.example.smartrecipe.ui.user.UserPreferenceActivity;
import com.example.smartrecipe.ui.voice.VoiceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private long userId;

    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> homeRecommend = new ArrayList<>();

    private RecipeAdapter recipeAdapter;

    private View sectionHome;
    private View sectionFeature;
    private View sectionMine;

    private EditText etSearch;
    private TextView tvSectionTitle;
    private TextView tvEmpty;
    private TextView tvCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 登录检查
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

        // Sections
        sectionHome = findViewById(R.id.sectionHome);
        sectionFeature = findViewById(R.id.sectionFeature);
        sectionMine = findViewById(R.id.sectionMine);

        // Home UI
        etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Feature UI
        Button btnRecognize = findViewById(R.id.btnRecognize);
        Button btnVoice = findViewById(R.id.btnVoice);
        Button btnPreference = findViewById(R.id.btnPreference);
        Button btnIngredientFilter = findViewById(R.id.btnIngredientFilter);

        // Mine UI
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnFavorite = findViewById(R.id.btnFavorite);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        // RecyclerView + Adapter（只初始化一次）
        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        });
        rv.setAdapter(recipeAdapter);

        // 读数据 + 初始推荐
        allRecipes = RecipeRepository.getAllRecipes(this);
        com.example.smartrecipe.data.local.entity.UserPreference pref = UserRepository.getPreference(this, userId);
        homeRecommend = PersonalizedRecommendEngine.recommend(
                allRecipes,
                pref,
                UserRepository.favoriteRecipeIds(this, userId),
                UserRepository.behaviorRecipeScores(this, userId),
                30
        );
        renderRecipes(homeRecommend, "为你推荐");

        // ====== listeners ======

        // 搜索
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            List<Recipe> searchResult = RecipeRepository.search(this, keyword);

            renderRecipes(searchResult, keyword.isEmpty() ? "全部食谱" : "搜索结果：" + keyword);

            if (!keyword.isEmpty()) {
                UserRepository.trackSearch(this, userId, keyword);
            }
            Toast.makeText(this, "共找到 " + (searchResult == null ? 0 : searchResult.size()) + " 个食谱", Toast.LENGTH_SHORT).show();
        });

        // Feature buttons
        btnRecognize.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));
        btnVoice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VoiceActivity.class)));
        btnPreference.setOnClickListener(v -> startActivity(new Intent(this, UserPreferenceActivity.class)));
        btnIngredientFilter.setOnClickListener(v -> openIngredientFilterDialog());

        // Mine buttons
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnFavorite.setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            SessionManager.logout(this);
            Intent it = new Intent(this, AuthActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
        });

        // Bottom nav
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showSection(0);
                return true;
            } else if (id == R.id.nav_feature) {
                showSection(1);
                return true;
            } else if (id == R.id.nav_mine) {
                showSection(2);
                refreshMineInfo();
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMineInfo();
    }

    private void showSection(int index) {
        sectionHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        sectionFeature.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        sectionMine.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    private void refreshMineInfo() {
        String username = UserRepository.currentUsername(this, userId);
        tvCurrentUser.setText("当前用户：" + (username == null ? "-" : username));
    }

    private void renderRecipes(List<Recipe> list, String sectionTitle) {
        tvSectionTitle.setText(sectionTitle);
        recipeAdapter.replaceData(list == null ? new ArrayList<>() : list);
        boolean empty = (list == null || list.isEmpty());
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void openIngredientFilterDialog() {
        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint("输入食材，多个用逗号分隔，如：番茄,鸡蛋");
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText et = new TextInputEditText(this);
        et.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        inputLayout.addView(et);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        inputLayout.setPadding(padding, 10, padding, 0);

        new MaterialAlertDialogBuilder(this)
                .setTitle("食材筛选")
                .setView(inputLayout)
                .setNegativeButton("取消", null)
                .setPositiveButton("筛选", (dialog, which) -> {
                    String text = et.getText() == null ? "" : et.getText().toString().trim();
                    List<Recipe> out = filterByIngredients(text);
                    renderRecipes(out, text.isEmpty() ? "为你推荐" : "食材筛选：" + text);

                    BottomNavigationView nav = findViewById(R.id.bottomNav);
                    nav.setSelectedItemId(R.id.nav_home);
                })
                .show();
    }

    private List<Recipe> filterByIngredients(String text) {
        if (text == null || text.isEmpty()) return homeRecommend;

        String[] tokens = text.replace("，", ",").split(",");
        List<Recipe> out = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (recipe.getIngredients() == null) continue;

            boolean allHit = true;
            for (String token : tokens) {
                String t = token.trim();
                if (t.isEmpty()) continue;

                boolean hit = false;
                for (String ing : recipe.getIngredients()) {
                    if (ing != null && ing.contains(t)) {
                        hit = true;
                        break;
                    }
                }
                if (!hit) {
                    allHit = false;
                    break;
                }
            }
            if (allHit) out.add(recipe);
        }
        return out;
    }
}
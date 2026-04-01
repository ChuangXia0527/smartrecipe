package com.example.smartrecipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_UI = "ui_prefs";
    private static final String CATEGORY_RECOMMEND = "推荐";
    private static final String CATEGORY_BREAKFAST = "早餐";
    private static final String CATEGORY_LUNCH = "午餐";
    private static final String CATEGORY_DINNER = "晚餐";
    private static final String CATEGORY_DESSERT = "甜点";

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
    private TextView tvMineTitle;
    private ImageView ivAvatar;

    private TextView chipRecommend;
    private TextView chipBreakfast;
    private TextView chipLunch;
    private TextView chipDinner;
    private TextView chipDessert;

    private final ActivityResultLauncher<String[]> avatarPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // ignore if provider does not allow persistable permission
                }
                saveAvatarUri(uri);
                showAvatar(uri);
            });

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

        sectionHome = findViewById(R.id.sectionHome);
        sectionFeature = findViewById(R.id.sectionFeature);
        sectionMine = findViewById(R.id.sectionMine);

        etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvEmpty = findViewById(R.id.tvEmpty);

        chipRecommend = findViewById(R.id.chipRecommend);
        chipBreakfast = findViewById(R.id.chipBreakfast);
        chipLunch = findViewById(R.id.chipLunch);
        chipDinner = findViewById(R.id.chipDinner);
        chipDessert = findViewById(R.id.chipDessert);

        Button btnRecognize = findViewById(R.id.btnRecognize);
        Button btnVoice = findViewById(R.id.btnVoice);
        Button btnPreference = findViewById(R.id.btnPreference);
        Button btnIngredientFilter = findViewById(R.id.btnIngredientFilter);

        tvMineTitle = findViewById(R.id.tvMineTitle);
        ivAvatar = findViewById(R.id.ivAvatar);
        Button btnProfile = findViewById(R.id.btnProfile);
        Button btnFavorite = findViewById(R.id.btnFavorite);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        recipeAdapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent it = new Intent(MainActivity.this, RecipeDetailActivity.class);
            it.putExtra("recipe_id", recipe.getId());
            startActivity(it);
        });
        rv.setAdapter(recipeAdapter);

        allRecipes = RecipeRepository.getAllRecipes(this);
        com.example.smartrecipe.data.local.entity.UserPreference pref = UserRepository.getPreference(this, userId);
        homeRecommend = PersonalizedRecommendEngine.recommend(
                allRecipes,
                userId,
                pref,
                UserRepository.favoriteRecipeIds(this, userId),
                UserRepository.behaviorRecipeScores(this, userId),
                UserRepository.behaviorRecipeScoresForAllUsers(this),
                30
        );
        applyCategoryFilter(CATEGORY_RECOMMEND);

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            List<Recipe> searchResult = RecipeRepository.search(this, keyword);
            renderRecipes(searchResult, keyword.isEmpty() ? "全部食谱" : "搜索结果：" + keyword);
            if (!keyword.isEmpty()) {
                UserRepository.trackSearch(this, userId, keyword);
            }
            Toast.makeText(this, "共找到 " + (searchResult == null ? 0 : searchResult.size()) + " 个食谱", Toast.LENGTH_SHORT).show();
        });

        btnRecognize.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecognizeActivity.class)));
        btnVoice.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, VoiceActivity.class)));
        btnPreference.setOnClickListener(v -> startActivity(new Intent(this, UserPreferenceActivity.class)));
        btnIngredientFilter.setOnClickListener(v -> openIngredientFilterDialog());

        ivAvatar.setOnClickListener(v -> avatarPickerLauncher.launch(new String[]{"image/*"}));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnFavorite.setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            SessionManager.logout(this);
            Intent it = new Intent(this, AuthActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
        });

        bindCategoryActions();

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

    private void bindCategoryActions() {
        chipRecommend.setOnClickListener(v -> applyCategoryFilter(CATEGORY_RECOMMEND));
        chipBreakfast.setOnClickListener(v -> applyCategoryFilter(CATEGORY_BREAKFAST));
        chipLunch.setOnClickListener(v -> applyCategoryFilter(CATEGORY_LUNCH));
        chipDinner.setOnClickListener(v -> applyCategoryFilter(CATEGORY_DINNER));
        chipDessert.setOnClickListener(v -> applyCategoryFilter(CATEGORY_DESSERT));
    }

    private void applyCategoryFilter(String category) {
        selectChip(category);
        List<Recipe> out;
        if (CATEGORY_RECOMMEND.equals(category)) {
            out = homeRecommend;
        } else {
            out = filterByCategory(category);
        }
        renderRecipes(out, "全部食谱");
    }

    private void selectChip(String category) {
        styleChip(chipRecommend, CATEGORY_RECOMMEND.equals(category));
        styleChip(chipBreakfast, CATEGORY_BREAKFAST.equals(category));
        styleChip(chipLunch, CATEGORY_LUNCH.equals(category));
        styleChip(chipDinner, CATEGORY_DINNER.equals(category));
        styleChip(chipDessert, CATEGORY_DESSERT.equals(category));
    }

    private void styleChip(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.bg_category_chip_active : R.drawable.bg_category_chip);
        int colorRes = active ? android.R.color.white : R.color.textSecondary;
        chip.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private List<Recipe> filterByCategory(String category) {
        List<Recipe> out = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (matchCategory(recipe, category)) {
                out.add(recipe);
            }
        }
        return out;
    }

    private boolean matchCategory(Recipe recipe, String category) {
        if (CATEGORY_BREAKFAST.equals(category)) {
            return hasTag(recipe, "早餐");
        }
        if (CATEGORY_DESSERT.equals(category)) {
            return hasAnyTag(recipe, Arrays.asList("甜品", "甜点"));
        }
        if (CATEGORY_LUNCH.equals(category)) {
            return hasAnyTag(recipe, Arrays.asList("主食", "下饭", "家常"));
        }
        if (CATEGORY_DINNER.equals(category)) {
            return hasAnyTag(recipe, Arrays.asList("清淡", "低脂", "高蛋白", "家常"));
        }
        return true;
    }

    private boolean hasTag(Recipe recipe, String target) {
        if (recipe.getTags() == null) return false;
        for (String tag : recipe.getTags()) {
            if (target.equals(tag)) return true;
        }
        return false;
    }

    private boolean hasAnyTag(Recipe recipe, List<String> targets) {
        if (recipe.getTags() == null) return false;
        for (String tag : recipe.getTags()) {
            if (targets.contains(tag)) return true;
        }
        return false;
    }

    private void showSection(int index) {
        sectionHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        sectionFeature.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        sectionMine.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    private void refreshMineInfo() {
        String username = UserRepository.currentUsername(this, userId);
        tvMineTitle.setText((username == null || username.trim().isEmpty()) ? "未登录用户" : username.trim());
        Uri avatarUri = getSavedAvatarUri();
        if (avatarUri != null) {
            showAvatar(avatarUri);
        } else {
            resetDefaultAvatar();
        }
    }

    private void showAvatar(Uri uri) {
        ivAvatar.setImageURI(uri);
        ivAvatar.setImageTintList(null);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAvatar.setPadding(0, 0, 0, 0);
    }

    private void resetDefaultAvatar() {
        ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        ivAvatar.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white)));
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        ivAvatar.setPadding(padding, padding, padding, padding);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private void saveAvatarUri(Uri uri) {
        SharedPreferences sp = getSharedPreferences(PREF_UI, MODE_PRIVATE);
        sp.edit().putString("avatar_uri_" + userId, uri.toString()).apply();
    }

    private Uri getSavedAvatarUri() {
        SharedPreferences sp = getSharedPreferences(PREF_UI, MODE_PRIVATE);
        String uriString = sp.getString("avatar_uri_" + userId, null);
        if (uriString == null || uriString.trim().isEmpty()) return null;
        return Uri.parse(uriString);
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
                    renderRecipes(out, text.isEmpty() ? "全部食谱" : "食材筛选：" + text);

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

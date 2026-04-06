package com.example.smartrecipe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.local.entity.IngredientInfo;
import com.example.smartrecipe.data.local.entity.ManagedRecipe;
import com.example.smartrecipe.data.local.entity.RecipeCategory;
import com.example.smartrecipe.data.repository.RecipeRepository;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.ui.auth.AuthActivity;

import java.util.List;

public class AdminRecipeIngredientActivity extends AppCompatActivity {
    private TextView tvRecipeList;
    private TextView tvCategoryList;
    private TextView tvIngredientList;

    private EditText etRecipeId, etRecipeName, etRecipeCategory, etRecipeIngredients, etRecipeSteps, etRecipeNutrition;
    private EditText etCategoryId, etCategoryName, etCategoryDesc;
    private EditText etIngredientId, etIngredientName, etIngredientNutrition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_recipe_ingredient_manage);

        tvRecipeList = findViewById(R.id.tvRecipeList);
        tvCategoryList = findViewById(R.id.tvCategoryList);
        tvIngredientList = findViewById(R.id.tvIngredientList);

        etRecipeId = findViewById(R.id.etRecipeId);
        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeCategory = findViewById(R.id.etRecipeCategory);
        etRecipeIngredients = findViewById(R.id.etRecipeIngredients);
        etRecipeSteps = findViewById(R.id.etRecipeSteps);
        etRecipeNutrition = findViewById(R.id.etRecipeNutrition);

        etCategoryId = findViewById(R.id.etCategoryId);
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDesc = findViewById(R.id.etCategoryDesc);

        etIngredientId = findViewById(R.id.etIngredientId);
        etIngredientName = findViewById(R.id.etIngredientName);
        etIngredientNutrition = findViewById(R.id.etIngredientNutrition);

        findViewById(R.id.btnAddRecipe).setOnClickListener(v -> {
            AdminRepository.addRecipe(this, t(etRecipeName), t(etRecipeCategory), t(etRecipeIngredients), t(etRecipeSteps), t(etRecipeNutrition));
            toast("已新增食谱");
            refreshAll();
        });
        findViewById(R.id.btnUpdateRecipe).setOnClickListener(v -> {
            boolean ok = AdminRepository.updateRecipe(this, l(etRecipeId), t(etRecipeName), t(etRecipeCategory), t(etRecipeIngredients), t(etRecipeSteps), t(etRecipeNutrition));
            toast(ok ? "已更新" : "更新失败");
            refreshAll();
        });
        findViewById(R.id.btnDeleteRecipe).setOnClickListener(v -> {
            boolean ok = AdminRepository.deleteRecipe(this, l(etRecipeId));
            toast(ok ? "已删除" : "删除失败");
            refreshAll();
        });

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> {
            AdminRepository.addCategory(this, t(etCategoryName), t(etCategoryDesc));
            refreshAll();
        });
        findViewById(R.id.btnUpdateCategory).setOnClickListener(v -> {
            boolean ok = AdminRepository.updateCategory(this, l(etCategoryId), t(etCategoryName), t(etCategoryDesc));
            toast(ok ? "已更新" : "失败");
            refreshAll();
        });
        findViewById(R.id.btnDeleteCategory).setOnClickListener(v -> {
            boolean ok = AdminRepository.deleteCategory(this, l(etCategoryId));
            toast(ok ? "已删除" : "失败");
            refreshAll();
        });

        findViewById(R.id.btnAddIngredient).setOnClickListener(v -> {
            AdminRepository.addIngredient(this, t(etIngredientName), t(etIngredientNutrition));
            refreshAll();
        });
        findViewById(R.id.btnUpdateIngredient).setOnClickListener(v -> {
            boolean ok = AdminRepository.updateIngredient(this, l(etIngredientId), t(etIngredientName), t(etIngredientNutrition));
            toast(ok ? "已更新" : "失败");
            refreshAll();
        });
        findViewById(R.id.btnDeleteIngredient).setOnClickListener(v -> {
            boolean ok = AdminRepository.deleteIngredient(this, l(etIngredientId));
            toast(ok ? "已删除" : "失败");
            refreshAll();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void refreshAll() {
        List<Recipe> systemRecipes = RecipeRepository.getAllRecipes(this);
        List<ManagedRecipe> managedRecipes = AdminRepository.allRecipes(this);
        StringBuilder recipeSb = new StringBuilder("系统食谱:\n");
        for (int i = 0; i < Math.min(20, systemRecipes.size()); i++) {
            Recipe r = systemRecipes.get(i);
            recipeSb.append("S-").append(r.getId()).append(" ").append(r.getTitle()).append("\n");
            recipeSb.append("S-").append(r.getId()).append(" ").append(r.getName()).append("\n");
        }
        recipeSb.append("\n管理员食谱:\n");
        for (ManagedRecipe r : managedRecipes) {
            recipeSb.append("A-").append(r.id).append(" ").append(r.name).append(" [").append(r.category).append("]\n");
        }
        tvRecipeList.setText(recipeSb.toString());

        List<RecipeCategory> categories = AdminRepository.allCategories(this);
        StringBuilder c = new StringBuilder();
        for (RecipeCategory item : categories) c.append(item.id).append(" ").append(item.name).append("\n");
        tvCategoryList.setText(c.length() == 0 ? "暂无分类" : c.toString());

        List<IngredientInfo> ingredients = AdminRepository.allIngredients(this);
        StringBuilder i = new StringBuilder();
        for (IngredientInfo item : ingredients) i.append(item.id).append(" ").append(item.name).append(" - ").append(item.nutrition).append("\n");
        tvIngredientList.setText(i.length() == 0 ? "暂无食材" : i.toString());
    }

    private String t(EditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private long l(EditText e) { try { return Long.parseLong(t(e)); } catch (Exception ignored) { return -1; } }
    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}

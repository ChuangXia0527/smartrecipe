package com.example.smartrecipe.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.smartrecipe.data.entity.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class RecipeRepository {

    public static List<Recipe> getAllRecipes(Context context) {
        try {
            // 获取 assets 文件夹中的 recipes.json
            InputStream inputStream = context.getAssets().open("recipes.json");
            InputStreamReader reader = new InputStreamReader(inputStream);

            // 使用 Gson 解析 JSON
            List<Recipe> recipes = new Gson().fromJson(reader, new TypeToken<List<Recipe>>() {}.getType());
            Log.d("RecipeRepository", "Loaded recipes: " + recipes.size());  // 输出加载的数据数量
            return recipes;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RecipeRepository", "Error reading recipes.json", e);
        }
        return Collections.emptyList(); // 如果加载失败，返回空列表
    }

    public static Recipe findById(Context context, int id) {
        List<Recipe> allRecipes = getAllRecipes(context);
        for (Recipe recipe : allRecipes) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        return null; // 如果没有找到，返回 null
    }
}

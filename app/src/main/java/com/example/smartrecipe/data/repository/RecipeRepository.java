package com.example.smartrecipe.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.smartrecipe.data.entity.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RecipeRepository {

    public static List<Recipe> getAllRecipes(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("recipes.json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            List<Recipe> recipes = new Gson().fromJson(reader, new TypeToken<List<Recipe>>() {}.getType());
            Log.d("RecipeRepository", "Loaded recipes: " + recipes.size());
            return recipes;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("RecipeRepository", "Error reading recipes.json", e);
        }
        return Collections.emptyList();
    }

    public static Recipe findById(Context context, int id) {
        List<Recipe> allRecipes = getAllRecipes(context);
        for (Recipe recipe : allRecipes) {
            if (recipe.getId() == id) return recipe;
        }
        return null;
    }

    public static List<Recipe> findByIds(Context context, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Recipe> all = getAllRecipes(context);
        List<Recipe> out = new ArrayList<>();
        for (Integer id : ids) {
            if (id == null) continue;
            for (Recipe recipe : all) {
                if (recipe.getId() == id) {
                    out.add(recipe);
                    break;
                }
            }
        }
        return out;
    }

    public static List<Recipe> search(Context context, String keyword) {
        List<Recipe> all = getAllRecipes(context);
        if (keyword == null || keyword.trim().isEmpty()) return all;

        String key = keyword.trim().toLowerCase(Locale.ROOT);
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : all) {
            if (recipe.getName() != null && recipe.getName().toLowerCase(Locale.ROOT).contains(key)) {
                result.add(recipe);
                continue;
            }

            if (recipe.getTags() != null) {
                boolean hit = false;
                for (String tag : recipe.getTags()) {
                    if (tag != null && tag.toLowerCase(Locale.ROOT).contains(key)) {
                        result.add(recipe);
                        hit = true;
                        break;
                    }
                }
                if (hit) continue;
            }

            if (recipe.getIngredients() != null) {
                for (String ing : recipe.getIngredients()) {
                    if (ing != null && ing.toLowerCase(Locale.ROOT).contains(key)) {
                        result.add(recipe);
                        break;
                    }
                }
            }
        }
        return result;
    }
}

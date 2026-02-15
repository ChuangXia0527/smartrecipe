package com.example.smartrecipe.ai.vision;

import android.content.Context;

import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.repository.RecipeRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class IngredientLexicon {

    private static Set<String> cached = null;

    public static Set<String> getAllIngredients(Context context) {
        if (cached != null) return cached;

        Set<String> set = new LinkedHashSet<>();
        List<Recipe> all = RecipeRepository.getAllRecipes(context);
        for (Recipe r : all) {
            if (r.getIngredients() == null) continue;
            for (String ing : r.getIngredients()) {
                if (ing == null) continue;
                String s = normalizeCn(ing);
                if (!s.isEmpty()) set.add(s);
            }
        }
        cached = set;
        return cached;
    }

    public static String normalizeCn(String s) {
        if (s == null) return "";
        // 去空格、统一全角符号
        return s.replace(" ", "")
                .replace("（", "(")
                .replace("）", ")")
                .trim();
    }
}

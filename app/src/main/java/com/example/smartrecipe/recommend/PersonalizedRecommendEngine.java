package com.example.smartrecipe.recommend;

import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.local.dao.UserBehaviorDao;
import com.example.smartrecipe.data.local.entity.UserPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PersonalizedRecommendEngine {

    public static List<Recipe> recommend(List<Recipe> all,
                                         UserPreference preference,
                                         List<Integer> favoriteIds,
                                         List<UserBehaviorDao.RecipeScoreRow> behaviorScores,
                                         int topN) {
        Map<Integer, Integer> favoriteBoost = new HashMap<>();
        for (Integer id : favoriteIds) favoriteBoost.put(id, 1);

        Map<Integer, Integer> behaviorBoost = new HashMap<>();
        for (UserBehaviorDao.RecipeScoreRow row : behaviorScores) {
            behaviorBoost.put(row.recipeId, row.cnt);
        }

        List<ScoredRecipe> list = new ArrayList<>();
        for (Recipe recipe : all) {
            int score = 1;

            // 冷启动：偏好与饮食类型
            if (preference != null) {
                if (containsPreference(recipe, preference.taste)) score += 5;
                if (containsPreference(recipe, preference.dietType)) score += 6;
                if (containsAnyIngredient(recipe, preference.commonIngredients)) score += 4;
                if (containsAnyIngredient(recipe, preference.allergies)) score -= 100;
            }

            // 热门/历史行为（可解释为“受欢迎程度 + 用户偏好行为”）
            score += behaviorBoost.getOrDefault(recipe.getId(), 0) * 2;
            score += favoriteBoost.getOrDefault(recipe.getId(), 0) * 8;

            // 偏好快手、低热量时的自然排序
            score += Math.max(0, 30 - recipe.getMinutes()) / 10;
            score += Math.max(0, 800 - recipe.getCalorie()) / 200;

            list.add(new ScoredRecipe(recipe, score));
        }

        list.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Recipe> out = new ArrayList<>();
        for (int i = 0; i < list.size() && i < topN; i++) out.add(list.get(i).recipe);
        return out;
    }

    private static boolean containsPreference(Recipe recipe, String pref) {
        if (pref == null || pref.trim().isEmpty()) return false;
        String p = pref.trim().toLowerCase(Locale.ROOT);
        if (recipe.getTags() != null) {
            for (String tag : recipe.getTags()) {
                if (tag.toLowerCase(Locale.ROOT).contains(p)) return true;
            }
        }
        return false;
    }

    private static boolean containsAnyIngredient(Recipe recipe, String text) {
        if (text == null || text.trim().isEmpty() || recipe.getIngredients() == null) return false;
        String[] tokens = text.replace("，", ",").split(",");
        for (String token : tokens) {
            String t = token.trim();
            if (t.isEmpty()) continue;
            for (String ing : recipe.getIngredients()) {
                if (ing.contains(t) || t.contains(ing)) return true;
            }
        }
        return false;
    }

    private static class ScoredRecipe {
        Recipe recipe;
        int score;

        ScoredRecipe(Recipe recipe, int score) {
            this.recipe = recipe;
            this.score = score;
        }
    }
}

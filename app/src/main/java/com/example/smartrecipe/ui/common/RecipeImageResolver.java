package com.example.smartrecipe.ui.common;

import android.content.Context;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;

public class RecipeImageResolver {

    /**
     * 图片放在 app/src/main/res/drawable/ 目录，命名规则：recipe_<id>
     */
    public static int resolveImageRes(Context context, Recipe recipe) {
        if (context == null || recipe == null)
            return R.drawable.recipe_img_default;

        String imageName = "recipe_" + recipe.getId();
        int resId = context.getResources()
                .getIdentifier(imageName, "drawable", context.getPackageName());

        return resId != 0 ? resId : R.drawable.recipe_img_default;
    }   // ✅ 这里补上结束大括号

    public static int resolveBackgroundRes(Recipe recipe) {
        String key = buildKey(recipe);

        if (containsAny(key, "鱼", "鲈", "三文鱼", "虾"))
            return R.drawable.recipe_img_fish;

        if (containsAny(key, "牛", "鸡", "猪", "肉", "肥牛", "鸡翅"))
            return R.drawable.recipe_img_meat;

        if (containsAny(key, "汤", "粥", "面"))
            return R.drawable.recipe_img_soup;

        if (containsAny(key, "蛋", "番茄"))
            return R.drawable.recipe_img_tomato;

        if (containsAny(key, "早餐", "燕麦", "酸奶", "水果"))
            return R.drawable.recipe_img_breakfast;

        if (containsAny(key, "菜", "菇", "豆腐", "西兰花", "黄瓜", "菠菜", "土豆"))
            return R.drawable.recipe_img_veggie;

        return R.drawable.recipe_img_default;
    }



    private static String buildKey(Recipe recipe) {
        if (recipe == null) return "";

        StringBuilder sb = new StringBuilder();

        if (recipe.getName() != null)
            sb.append(recipe.getName());

        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                sb.append(" ").append(ingredient);
            }
        }

        if (recipe.getTags() != null) {
            for (String tag : recipe.getTags()) {
                sb.append(" ").append(tag);
            }
        }

        return sb.toString();
    }

    private static boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) return true;
        }
        return false;
    }
}
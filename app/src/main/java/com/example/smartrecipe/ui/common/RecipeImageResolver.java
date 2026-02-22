package com.example.smartrecipe.ui.common;

import android.content.Context;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.entity.Recipe;

public class RecipeImageResolver {

    /**
     * 图片放在 app/src/main/res/drawable/ 目录，命名规则：recipe_<id>
     * 例如 id=1 -> recipe_1.png / recipe_1.webp / recipe_1.jpg(不建议)
     */
    public static int resolveImageRes(Context context, Recipe recipe) {
        if (context == null || recipe == null) return R.drawable.recipe_img_default;

        String imageName = "recipe_" + recipe.getId();
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.recipe_img_default;
    }
}

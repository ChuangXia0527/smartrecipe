package com.example.smartrecipe.data.user;

import android.content.Context;

import com.example.smartrecipe.data.local.AppDatabase;
import com.example.smartrecipe.data.local.entity.FavoriteRecipe;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.local.entity.UserBehavior;
import com.example.smartrecipe.data.local.entity.UserPreference;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserRepository {

    public static UserAccount register(Context context, String username, String password) {
        AppDatabase db = AppDatabase.get(context);
        if (db.userAccountDao().findByUsername(username) != null) {
            return null;
        }
        long id = db.userAccountDao().insert(new UserAccount(username, password));
        return db.userAccountDao().findById(id);
    }

    public static UserAccount login(Context context, String username, String password) {
        UserAccount account = AppDatabase.get(context).userAccountDao().findByUsername(username);
        if (account == null) return null;
        return account.password.equals(password) ? account : null;
    }

    public static String currentUsername(Context context, long userId) {
        UserAccount account = AppDatabase.get(context).userAccountDao().findById(userId);
        return account == null ? null : account.username;
    }

    public static boolean updateUserCredentials(Context context, long userId, String newUsername, String newPassword) {
        AppDatabase db = AppDatabase.get(context);
        UserAccount account = db.userAccountDao().findById(userId);
        if (account == null) return false;

        UserAccount sameName = db.userAccountDao().findByUsername(newUsername);
        if (sameName != null && sameName.id != userId) return false;

        return db.userAccountDao().updateCredentials(userId, newUsername, newPassword) > 0;
    }

    public static void savePreference(Context context, long userId, String taste, String allergies, String dietType, String commonIngredients) {
        AppDatabase.get(context).userPreferenceDao().upsert(
                new UserPreference(userId, taste, allergies, dietType, commonIngredients)
        );
    }

    public static UserPreference getPreference(Context context, long userId) {
        return AppDatabase.get(context).userPreferenceDao().findByUserId(userId);
    }

    public static void addFavorite(Context context, long userId, int recipeId) {
        AppDatabase.get(context).favoriteRecipeDao().add(new FavoriteRecipe(userId, recipeId, System.currentTimeMillis()));
    }

    public static void removeFavorite(Context context, long userId, int recipeId) {
        AppDatabase.get(context).favoriteRecipeDao().remove(userId, recipeId);
    }

    public static boolean isFavorite(Context context, long userId, int recipeId) {
        return AppDatabase.get(context).favoriteRecipeDao().countFavorite(userId, recipeId) > 0;
    }

    public static List<Integer> favoriteRecipeIds(Context context, long userId) {
        return AppDatabase.get(context).favoriteRecipeDao().listFavoriteRecipeIds(userId);
    }

    public static void trackRecipeOpen(Context context, long userId, int recipeId) {
        AppDatabase.get(context).userBehaviorDao().insert(new UserBehavior(userId, recipeId, "VIEW", null, System.currentTimeMillis()));
    }

    public static void trackSearch(Context context, long userId, String keyword) {
        AppDatabase.get(context).userBehaviorDao().insert(new UserBehavior(userId, -1, "SEARCH", keyword, System.currentTimeMillis()));
    }

    public static List<Integer> recentViewedRecipeIds(Context context, long userId, int limit) {
        List<Integer> ids = AppDatabase.get(context).userBehaviorDao().recentRecipeIds(userId, "VIEW", limit * 3);
        Set<Integer> dedup = new LinkedHashSet<>(ids);
        List<Integer> out = new ArrayList<>(dedup);
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    public static List<UserBehavior> recentBehaviors(Context context, long userId, int limit) {
        return AppDatabase.get(context).userBehaviorDao().recentBehaviors(userId, limit);
    }

    public static List<com.example.smartrecipe.data.local.dao.UserBehaviorDao.RecipeScoreRow> behaviorRecipeScores(Context context, long userId) {
        return AppDatabase.get(context).userBehaviorDao().behaviorRecipeScores(userId);
    }
}

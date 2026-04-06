package com.example.smartrecipe.data.user;

import android.content.Context;

import com.example.smartrecipe.data.local.AppDatabase;
import com.example.smartrecipe.data.local.entity.FavoriteRecipe;
import com.example.smartrecipe.data.local.entity.InventoryItem;
import com.example.smartrecipe.data.local.entity.PurchaseRecord;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.local.entity.UserFeedback;
import com.example.smartrecipe.data.local.entity.UserBehavior;
import com.example.smartrecipe.data.local.entity.UserPreference;
import com.example.smartrecipe.data.local.entity.UserProfileExtra;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UserRepository {

    private static final long EFFECTIVE_VIEW_THRESHOLD_SECONDS = 3L;

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

    public static boolean updateUsername(Context context, long userId, String newUsername) {
        AppDatabase db = AppDatabase.get(context);
        UserAccount account = db.userAccountDao().findById(userId);
        if (account == null) return false;

        UserAccount sameName = db.userAccountDao().findByUsername(newUsername);
        if (sameName != null && sameName.id != userId) return false;

        return db.userAccountDao().updateUsername(userId, newUsername) > 0;
    }

    public static boolean updatePassword(Context context, long userId, String newPassword) {
        AppDatabase db = AppDatabase.get(context);
        UserAccount account = db.userAccountDao().findById(userId);
        if (account == null) return false;

        return db.userAccountDao().updatePassword(userId, newPassword) > 0;
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
        trackBehavior(context, userId, recipeId, "FAVORITE", "1");
    }

    public static void removeFavorite(Context context, long userId, int recipeId) {
        AppDatabase.get(context).favoriteRecipeDao().remove(userId, recipeId);
        trackBehavior(context, userId, recipeId, "FAVORITE", "-1");
    }

    public static boolean isFavorite(Context context, long userId, int recipeId) {
        return AppDatabase.get(context).favoriteRecipeDao().countFavorite(userId, recipeId) > 0;
    }

    public static List<Integer> favoriteRecipeIds(Context context, long userId) {
        return AppDatabase.get(context).favoriteRecipeDao().listFavoriteRecipeIds(userId);
    }

    public static void trackRecipeOpen(Context context, long userId, int recipeId) {
        trackBehavior(context, userId, recipeId, "VIEW", null);
    }

    public static void trackRecipeViewDuration(Context context, long userId, int recipeId, long durationSeconds) {
        if (durationSeconds < EFFECTIVE_VIEW_THRESHOLD_SECONDS) return;
        trackBehavior(context, userId, recipeId, "VIEW_DURATION", String.valueOf(durationSeconds));
    }

    public static void rateRecipe(Context context, long userId, int recipeId, int rating) {
        if (rating < 1 || rating > 5) return;
        trackBehavior(context, userId, recipeId, "RATE", String.valueOf(rating));
    }

    public static void trackSearch(Context context, long userId, String keyword) {
        trackBehavior(context, userId, -1, "SEARCH", keyword);
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


    public static void saveProfileExtra(Context context, long userId, String contact, int familySize, String dietaryTaboo) {
        AppDatabase.get(context).userProfileExtraDao().upsert(
                new UserProfileExtra(userId, contact, familySize, dietaryTaboo)
        );
    }

    public static UserProfileExtra getProfileExtra(Context context, long userId) {
        return AppDatabase.get(context).userProfileExtraDao().findByUserId(userId);
    }

    public static void addPurchaseRecord(Context context, long userId, String ingredientName, double quantity, double price) {
        AppDatabase.get(context).purchaseRecordDao().insert(
                new PurchaseRecord(userId, ingredientName, quantity, price, System.currentTimeMillis())
        );
    }

    public static List<PurchaseRecord> recentPurchaseRecords(Context context, long userId, int limit) {
        return AppDatabase.get(context).purchaseRecordDao().recentByUser(userId, limit);
    }

    public static void addInventoryItem(Context context, long userId, String ingredientName, double quantity, double lowStockThreshold) {
        AppDatabase.get(context).inventoryItemDao().insert(
                new InventoryItem(userId, ingredientName, quantity, lowStockThreshold, System.currentTimeMillis())
        );
    }

    public static List<InventoryItem> inventoryItems(Context context, long userId) {
        return AppDatabase.get(context).inventoryItemDao().listByUser(userId);
    }

    public static boolean updateInventoryQuantity(Context context, long itemId, double quantity) {
        return AppDatabase.get(context).inventoryItemDao().updateQuantity(itemId, quantity, System.currentTimeMillis()) > 0;
    }

    public static void submitFeedback(Context context, long userId, String feedbackType, String content) {
        AppDatabase.get(context).userFeedbackDao().insert(
                new UserFeedback(userId, feedbackType, content, "已提交", "感谢反馈，我们会尽快处理。", System.currentTimeMillis(), 0L)
        );
    }

    public static List<UserFeedback> feedbackList(Context context, long userId) {
        return AppDatabase.get(context).userFeedbackDao().listByUser(userId);
    }

    public static PurchaseStats purchaseStats(Context context, long userId) {
        List<PurchaseRecord> all = AppDatabase.get(context).purchaseRecordDao().allByUser(userId);
        double monthlyCost = 0;
        long now = System.currentTimeMillis();
        long monthStart = now - 30L * 24 * 60 * 60 * 1000;
        Map<String, Integer> counter = new HashMap<>();
        for (PurchaseRecord record : all) {
            if (record.purchasedAt >= monthStart) monthlyCost += record.price;
            String name = record.ingredientName == null ? "" : record.ingredientName.trim();
            if (name.isEmpty()) continue;
            Integer count = counter.get(name);
            counter.put(name, count == null ? 1 : count + 1);
        }
        String topIngredient = "暂无";
        int topCount = 0;
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            if (entry.getValue() > topCount) {
                topIngredient = entry.getKey();
                topCount = entry.getValue();
            }
        }
        return new PurchaseStats(String.format(Locale.getDefault(), "%.2f", monthlyCost), topIngredient, topCount);
    }

    public static class PurchaseStats {
        public final String monthlyCost;
        public final String topIngredient;
        public final int topCount;

        public PurchaseStats(String monthlyCost, String topIngredient, int topCount) {
            this.monthlyCost = monthlyCost;
            this.topIngredient = topIngredient;
            this.topCount = topCount;
        }
    }

    public static List<com.example.smartrecipe.data.local.dao.UserBehaviorDao.RecipeScoreRow> behaviorRecipeScores(Context context, long userId) {
        return AppDatabase.get(context).userBehaviorDao().behaviorRecipeScores(userId);
    }

    public static List<com.example.smartrecipe.data.local.dao.UserBehaviorDao.UserRecipeScoreRow> behaviorRecipeScoresForAllUsers(Context context) {
        return AppDatabase.get(context).userBehaviorDao().behaviorRecipeScoresForAllUsers();
    }

    private static void trackBehavior(Context context, long userId, int recipeId, String actionType, String keyword) {
        AppDatabase.get(context).userBehaviorDao().insert(
                new UserBehavior(userId, recipeId, actionType, keyword, System.currentTimeMillis())
        );
    }
}

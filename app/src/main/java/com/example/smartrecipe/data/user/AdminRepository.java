package com.example.smartrecipe.data.user;

import android.content.Context;

import com.example.smartrecipe.data.local.AppDatabase;
import com.example.smartrecipe.data.local.entity.AiConfig;
import com.example.smartrecipe.data.local.entity.IngredientInfo;
import com.example.smartrecipe.data.local.entity.ManagedRecipe;
import com.example.smartrecipe.data.local.entity.PurchaseRecord;
import com.example.smartrecipe.data.local.entity.RecipeCategory;
import com.example.smartrecipe.data.local.entity.SystemAnnouncement;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.local.entity.UserFeedback;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminRepository {

    public static List<UserAccount> allUsers(Context context) {
        return AppDatabase.get(context).userAccountDao().listAll();
    }

    public static boolean setUserDisabled(Context context, long userId, boolean disabled) {
        return AppDatabase.get(context).userAccountDao().updateDisabled(userId, disabled ? 1 : 0) > 0;
    }

    public static boolean resetPassword(Context context, long userId, String newPassword) {
        return AppDatabase.get(context).userAccountDao().updatePassword(userId, newPassword) > 0;
    }

    public static void addRecipe(Context context, String name, String category, String ingredients, String steps, String nutrition) {
        AppDatabase.get(context).managedRecipeDao().insert(new ManagedRecipe(name, category, ingredients, steps, nutrition));
    }

    public static boolean updateRecipe(Context context, long id, String name, String category, String ingredients, String steps, String nutrition) {
        return AppDatabase.get(context).managedRecipeDao().update(id, name, category, ingredients, steps, nutrition) > 0;
    }

    public static boolean deleteRecipe(Context context, long id) {
        return AppDatabase.get(context).managedRecipeDao().delete(id) > 0;
    }

    public static List<ManagedRecipe> allRecipes(Context context) {
        return AppDatabase.get(context).managedRecipeDao().listAll();
    }

    public static void addCategory(Context context, String name, String description) {
        AppDatabase.get(context).recipeCategoryDao().insert(new RecipeCategory(name, description));
    }

    public static boolean updateCategory(Context context, long id, String name, String description) {
        return AppDatabase.get(context).recipeCategoryDao().update(id, name, description) > 0;
    }

    public static boolean deleteCategory(Context context, long id) {
        return AppDatabase.get(context).recipeCategoryDao().delete(id) > 0;
    }

    public static List<RecipeCategory> allCategories(Context context) {
        return AppDatabase.get(context).recipeCategoryDao().listAll();
    }

    public static void addIngredient(Context context, String name, String nutrition) {
        AppDatabase.get(context).ingredientInfoDao().insert(new IngredientInfo(name, nutrition));
    }

    public static boolean updateIngredient(Context context, long id, String name, String nutrition) {
        return AppDatabase.get(context).ingredientInfoDao().update(id, name, nutrition) > 0;
    }

    public static boolean deleteIngredient(Context context, long id) {
        return AppDatabase.get(context).ingredientInfoDao().delete(id) > 0;
    }

    public static List<IngredientInfo> allIngredients(Context context) {
        return AppDatabase.get(context).ingredientInfoDao().listAll();
    }

    public static List<PurchaseRecord> allPurchaseRecords(Context context) {
        return AppDatabase.get(context).purchaseRecordDao().allRecords();
    }

    public static String exportPurchaseCsv(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,userId,ingredient,quantity,price,purchasedAt\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (PurchaseRecord record : allPurchaseRecords(context)) {
            sb.append(record.id).append(',')
                    .append(record.userId).append(',')
                    .append(record.ingredientName).append(',')
                    .append(record.quantity).append(',')
                    .append(record.price).append(',')
                    .append(sdf.format(record.purchasedAt)).append('\n');
        }
        return sb.toString();
    }

    public static void saveAiConfig(Context context, double similarityThreshold, double preferenceWeight, String nutritionBase, String tabooBase) {
        AppDatabase.get(context).aiConfigDao().upsert(new AiConfig(1, similarityThreshold, preferenceWeight, nutritionBase, tabooBase));
    }

    public static AiConfig getAiConfig(Context context) {
        AiConfig config = AppDatabase.get(context).aiConfigDao().getMainConfig();
        if (config == null) {
            config = new AiConfig(1, 0.6, 0.4, "基础营养库", "基础禁忌库");
            AppDatabase.get(context).aiConfigDao().upsert(config);
        }
        return config;
    }

    public static void publishAnnouncement(Context context, String title, String content) {
        AppDatabase.get(context).systemAnnouncementDao().insert(new SystemAnnouncement(title, content, System.currentTimeMillis()));
    }

    public static List<SystemAnnouncement> announcements(Context context) {
        return AppDatabase.get(context).systemAnnouncementDao().listAll();
    }

    public static List<UserFeedback> allFeedback(Context context) {
        return AppDatabase.get(context).userFeedbackDao().listAll();
    }

    public static boolean processFeedback(Context context, long feedbackId, String status, String reply) {
        return AppDatabase.get(context).userFeedbackDao().process(feedbackId, status, reply, System.currentTimeMillis()) > 0;
    }
}

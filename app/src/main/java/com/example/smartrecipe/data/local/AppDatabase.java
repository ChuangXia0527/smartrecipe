package com.example.smartrecipe.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartrecipe.data.local.dao.FavoriteRecipeDao;
import com.example.smartrecipe.data.local.dao.SystemAnnouncementDao;
import com.example.smartrecipe.data.local.dao.RecipeCategoryDao;
import com.example.smartrecipe.data.local.dao.ManagedRecipeDao;
import com.example.smartrecipe.data.local.dao.IngredientInfoDao;
import com.example.smartrecipe.data.local.dao.AiConfigDao;
import com.example.smartrecipe.data.local.dao.InventoryItemDao;
import com.example.smartrecipe.data.local.dao.PurchaseRecordDao;
import com.example.smartrecipe.data.local.dao.UserAccountDao;
import com.example.smartrecipe.data.local.dao.UserFeedbackDao;
import com.example.smartrecipe.data.local.dao.UserBehaviorDao;
import com.example.smartrecipe.data.local.dao.UserProfileExtraDao;
import com.example.smartrecipe.data.local.dao.UserPreferenceDao;
import com.example.smartrecipe.data.local.entity.FavoriteRecipe;
import com.example.smartrecipe.data.local.entity.SystemAnnouncement;
import com.example.smartrecipe.data.local.entity.RecipeCategory;
import com.example.smartrecipe.data.local.entity.ManagedRecipe;
import com.example.smartrecipe.data.local.entity.IngredientInfo;
import com.example.smartrecipe.data.local.entity.AiConfig;
import com.example.smartrecipe.data.local.entity.InventoryItem;
import com.example.smartrecipe.data.local.entity.PurchaseRecord;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.local.entity.UserFeedback;
import com.example.smartrecipe.data.local.entity.UserBehavior;
import com.example.smartrecipe.data.local.entity.UserProfileExtra;
import com.example.smartrecipe.data.local.entity.UserPreference;

@Database(entities = {UserAccount.class, UserPreference.class, FavoriteRecipe.class, UserBehavior.class, UserProfileExtra.class, PurchaseRecord.class, InventoryItem.class, UserFeedback.class, ManagedRecipe.class, RecipeCategory.class, IngredientInfo.class, SystemAnnouncement.class, AiConfig.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserAccountDao userAccountDao();
    public abstract UserPreferenceDao userPreferenceDao();
    public abstract FavoriteRecipeDao favoriteRecipeDao();
    public abstract UserBehaviorDao userBehaviorDao();
    public abstract UserProfileExtraDao userProfileExtraDao();
    public abstract PurchaseRecordDao purchaseRecordDao();
    public abstract InventoryItemDao inventoryItemDao();
    public abstract UserFeedbackDao userFeedbackDao();
    public abstract ManagedRecipeDao managedRecipeDao();
    public abstract RecipeCategoryDao recipeCategoryDao();
    public abstract IngredientInfoDao ingredientInfoDao();
    public abstract SystemAnnouncementDao systemAnnouncementDao();
    public abstract AiConfigDao aiConfigDao();

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "smartrecipe.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

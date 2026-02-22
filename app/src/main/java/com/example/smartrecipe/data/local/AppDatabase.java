package com.example.smartrecipe.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartrecipe.data.local.dao.FavoriteRecipeDao;
import com.example.smartrecipe.data.local.dao.UserAccountDao;
import com.example.smartrecipe.data.local.dao.UserBehaviorDao;
import com.example.smartrecipe.data.local.dao.UserPreferenceDao;
import com.example.smartrecipe.data.local.entity.FavoriteRecipe;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.local.entity.UserBehavior;
import com.example.smartrecipe.data.local.entity.UserPreference;

@Database(entities = {UserAccount.class, UserPreference.class, FavoriteRecipe.class, UserBehavior.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserAccountDao userAccountDao();
    public abstract UserPreferenceDao userPreferenceDao();
    public abstract FavoriteRecipeDao favoriteRecipeDao();
    public abstract UserBehaviorDao userBehaviorDao();

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

package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.UserPreference;

@Dao
public interface UserPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserPreference preference);

    @Query("SELECT * FROM user_preference WHERE userId = :userId LIMIT 1")
    UserPreference findByUserId(long userId);
}

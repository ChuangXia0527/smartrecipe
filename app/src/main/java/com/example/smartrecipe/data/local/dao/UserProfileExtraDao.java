package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.UserProfileExtra;

@Dao
public interface UserProfileExtraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserProfileExtra profileExtra);

    @Query("SELECT * FROM user_profile_extra WHERE userId = :userId LIMIT 1")
    UserProfileExtra findByUserId(long userId);
}

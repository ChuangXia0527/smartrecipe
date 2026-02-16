package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.UserAccount;

@Dao
public interface UserAccountDao {
    @Insert
    long insert(UserAccount userAccount);

    @Query("SELECT * FROM user_account WHERE username = :username LIMIT 1")
    UserAccount findByUsername(String username);

    @Query("SELECT * FROM user_account WHERE id = :id LIMIT 1")
    UserAccount findById(long id);
}

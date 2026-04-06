package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.UserAccount;

import java.util.List;

@Dao
public interface UserAccountDao {
    @Insert
    long insert(UserAccount userAccount);

    @Query("SELECT * FROM user_account WHERE username = :username LIMIT 1")
    UserAccount findByUsername(String username);

    @Query("SELECT * FROM user_account WHERE id = :id LIMIT 1")
    UserAccount findById(long id);

    @Query("SELECT * FROM user_account ORDER BY id DESC")
    List<UserAccount> listAll();

    @Query("UPDATE user_account SET username = :username, password = :password WHERE id = :id")
    int updateCredentials(long id, String username, String password);

    @Query("UPDATE user_account SET username = :username WHERE id = :id")
    int updateUsername(long id, String username);

    @Query("UPDATE user_account SET password = :password WHERE id = :id")
    int updatePassword(long id, String password);

    @Query("UPDATE user_account SET disabled = :disabled WHERE id = :id")
    int updateDisabled(long id, int disabled);
}

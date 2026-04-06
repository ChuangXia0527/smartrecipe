package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.UserFeedback;

import java.util.List;

@Dao
public interface UserFeedbackDao {

    @Insert
    long insert(UserFeedback feedback);

    @Query("SELECT * FROM user_feedback WHERE userId = :userId ORDER BY createdAt DESC")
    List<UserFeedback> listByUser(long userId);

    @Query("SELECT * FROM user_feedback ORDER BY createdAt DESC")
    List<UserFeedback> listAll();

    @Query("UPDATE user_feedback SET status = :status, reply = :reply, repliedAt = :repliedAt WHERE id = :id")
    int process(long id, String status, String reply, long repliedAt);
}

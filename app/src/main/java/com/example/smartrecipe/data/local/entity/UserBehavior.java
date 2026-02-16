package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_behavior")
public class UserBehavior {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public int recipeId;
    public String actionType;
    public String keyword;
    public long createdAt;

    public UserBehavior(long userId, int recipeId, String actionType, String keyword, long createdAt) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.actionType = actionType;
        this.keyword = keyword;
        this.createdAt = createdAt;
    }
}

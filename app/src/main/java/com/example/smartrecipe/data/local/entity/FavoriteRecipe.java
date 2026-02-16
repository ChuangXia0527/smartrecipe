package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_recipe", primaryKeys = {"userId", "recipeId"})
public class FavoriteRecipe {
    public long userId;
    public int recipeId;
    public long createdAt;

    public FavoriteRecipe(long userId, int recipeId, long createdAt) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.createdAt = createdAt;
    }
}

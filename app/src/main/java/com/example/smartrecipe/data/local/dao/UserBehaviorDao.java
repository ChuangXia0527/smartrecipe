package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.UserBehavior;

import java.util.List;

@Dao
public interface UserBehaviorDao {
    @Insert
    void insert(UserBehavior behavior);

    @Query("SELECT recipeId FROM user_behavior WHERE userId = :userId AND actionType = :actionType AND recipeId > 0 ORDER BY createdAt DESC LIMIT :limit")
    List<Integer> recentRecipeIds(long userId, String actionType, int limit);

    @Query("SELECT * FROM user_behavior WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    List<UserBehavior> recentBehaviors(long userId, int limit);

    @Query("SELECT recipeId, COUNT(1) as cnt FROM user_behavior WHERE userId = :userId AND recipeId > 0 GROUP BY recipeId ORDER BY cnt DESC")
    List<RecipeScoreRow> behaviorRecipeScores(long userId);

    class RecipeScoreRow {
        public int recipeId;
        public int cnt;
    }
}

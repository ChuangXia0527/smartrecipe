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

    @Query("SELECT recipeId, " +
            "SUM(CASE WHEN actionType = 'VIEW' THEN 1 ELSE 0 END) as cnt, " +
            "SUM(CASE WHEN actionType = 'FAVORITE' AND keyword = '1' THEN 1 WHEN actionType = 'FAVORITE' AND keyword = '-1' THEN -1 ELSE 0 END) as favoriteDelta, " +
            "SUM(CASE WHEN actionType = 'RATE' THEN CAST(keyword AS INTEGER) - 3 ELSE 0 END) as ratingDelta, " +
            "SUM(CASE WHEN actionType = 'VIEW_DURATION' THEN CAST(keyword AS INTEGER) ELSE 0 END) as totalViewDuration " +
            "FROM user_behavior WHERE userId = :userId AND recipeId > 0 GROUP BY recipeId ORDER BY cnt DESC")
    List<RecipeScoreRow> behaviorRecipeScores(long userId);

    class RecipeScoreRow {
        public int recipeId;
        public int cnt;
        public int favoriteDelta;
        public int ratingDelta;
        public int totalViewDuration;
    }
}

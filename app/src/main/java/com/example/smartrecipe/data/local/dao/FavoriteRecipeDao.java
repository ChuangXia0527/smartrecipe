package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.FavoriteRecipe;

import java.util.List;

@Dao
public interface FavoriteRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(FavoriteRecipe favoriteRecipe);

    @Query("DELETE FROM favorite_recipe WHERE userId = :userId AND recipeId = :recipeId")
    void remove(long userId, int recipeId);

    @Query("SELECT recipeId FROM favorite_recipe WHERE userId = :userId ORDER BY createdAt DESC")
    List<Integer> listFavoriteRecipeIds(long userId);

    @Query("SELECT COUNT(1) FROM favorite_recipe WHERE userId = :userId AND recipeId = :recipeId")
    int countFavorite(long userId, int recipeId);
}

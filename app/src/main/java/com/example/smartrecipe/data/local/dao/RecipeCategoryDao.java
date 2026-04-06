package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.RecipeCategory;

import java.util.List;

@Dao
public interface RecipeCategoryDao {
    @Insert
    long insert(RecipeCategory category);

    @Query("SELECT * FROM recipe_category ORDER BY id DESC")
    List<RecipeCategory> listAll();

    @Query("UPDATE recipe_category SET name=:name, description=:description WHERE id=:id")
    int update(long id, String name, String description);

    @Query("DELETE FROM recipe_category WHERE id=:id")
    int delete(long id);
}

package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.ManagedRecipe;

import java.util.List;

@Dao
public interface ManagedRecipeDao {
    @Insert
    long insert(ManagedRecipe recipe);

    @Query("SELECT * FROM managed_recipe ORDER BY id DESC")
    List<ManagedRecipe> listAll();

    @Query("UPDATE managed_recipe SET name=:name, category=:category, ingredients=:ingredients, steps=:steps, nutrition=:nutrition WHERE id=:id")
    int update(long id, String name, String category, String ingredients, String steps, String nutrition);

    @Query("DELETE FROM managed_recipe WHERE id=:id")
    int delete(long id);
}

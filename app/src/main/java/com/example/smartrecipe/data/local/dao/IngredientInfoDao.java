package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.IngredientInfo;

import java.util.List;

@Dao
public interface IngredientInfoDao {
    @Insert
    long insert(IngredientInfo info);

    @Query("SELECT * FROM ingredient_info ORDER BY id DESC")
    List<IngredientInfo> listAll();

    @Query("UPDATE ingredient_info SET name=:name, nutrition=:nutrition WHERE id=:id")
    int update(long id, String name, String nutrition);

    @Query("DELETE FROM ingredient_info WHERE id=:id")
    int delete(long id);
}

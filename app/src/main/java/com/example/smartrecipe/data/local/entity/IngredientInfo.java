package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingredient_info")
public class IngredientInfo {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String nutrition;

    public IngredientInfo(String name, String nutrition) {
        this.name = name;
        this.nutrition = nutrition;
    }
}

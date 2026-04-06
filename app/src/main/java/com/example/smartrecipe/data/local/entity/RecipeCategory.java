package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipe_category")
public class RecipeCategory {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String description;

    public RecipeCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

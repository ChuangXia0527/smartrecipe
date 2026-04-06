package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "managed_recipe")
public class ManagedRecipe {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String category;
    public String ingredients;
    public String steps;
    public String nutrition;

    public ManagedRecipe(String name, String category, String ingredients, String steps, String nutrition) {
        this.name = name;
        this.category = category;
        this.ingredients = ingredients;
        this.steps = steps;
        this.nutrition = nutrition;
    }
}

package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_preference")
public class UserPreference {
    @PrimaryKey
    public long userId;
    public String taste;
    public String allergies;
    public String dietType;
    public String commonIngredients;

    public UserPreference(long userId, String taste, String allergies, String dietType, String commonIngredients) {
        this.userId = userId;
        this.taste = taste;
        this.allergies = allergies;
        this.dietType = dietType;
        this.commonIngredients = commonIngredients;
    }
}

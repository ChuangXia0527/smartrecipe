package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile_extra")
public class UserProfileExtra {
    @PrimaryKey
    public long userId;
    public String contact;
    public int familySize;
    public String dietaryTaboo;

    public UserProfileExtra(long userId, String contact, int familySize, String dietaryTaboo) {
        this.userId = userId;
        this.contact = contact;
        this.familySize = familySize;
        this.dietaryTaboo = dietaryTaboo;
    }
}

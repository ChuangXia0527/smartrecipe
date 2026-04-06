package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_account", indices = {@Index(value = {"username"}, unique = true)})
public class UserAccount {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String username;
    public String password;
    public int disabled;

    public UserAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.disabled = 0;
    }
}

package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "system_announcement")
public class SystemAnnouncement {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String content;
    public long createdAt;

    public SystemAnnouncement(String title, String content, long createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}

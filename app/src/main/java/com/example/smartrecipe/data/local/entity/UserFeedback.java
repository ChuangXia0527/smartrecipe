package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_feedback")
public class UserFeedback {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String feedbackType;
    public String content;
    public String status;
    public String reply;
    public long createdAt;
    public long repliedAt;

    public UserFeedback(long userId, String feedbackType, String content, String status, String reply, long createdAt, long repliedAt) {
        this.userId = userId;
        this.feedbackType = feedbackType;
        this.content = content;
        this.status = status;
        this.reply = reply;
        this.createdAt = createdAt;
        this.repliedAt = repliedAt;
    }
}

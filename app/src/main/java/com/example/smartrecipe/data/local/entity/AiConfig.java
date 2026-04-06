package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ai_config")
public class AiConfig {
    @PrimaryKey
    public int id;
    public double similarityThreshold;
    public double preferenceWeight;
    public String nutritionBase;
    public String tabooBase;

    public AiConfig(int id, double similarityThreshold, double preferenceWeight, String nutritionBase, String tabooBase) {
        this.id = id;
        this.similarityThreshold = similarityThreshold;
        this.preferenceWeight = preferenceWeight;
        this.nutritionBase = nutritionBase;
        this.tabooBase = tabooBase;
    }
}

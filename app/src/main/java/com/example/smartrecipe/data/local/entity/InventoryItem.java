package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_item")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String ingredientName;
    public double quantity;
    public double lowStockThreshold;
    public long updatedAt;

    public InventoryItem(long userId, String ingredientName, double quantity, double lowStockThreshold, long updatedAt) {
        this.userId = userId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.updatedAt = updatedAt;
    }
}

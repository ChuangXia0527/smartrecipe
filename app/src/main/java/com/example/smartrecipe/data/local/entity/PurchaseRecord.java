package com.example.smartrecipe.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchase_record")
public class PurchaseRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String ingredientName;
    public double quantity;
    public double price;
    public long purchasedAt;

    public PurchaseRecord(long userId, String ingredientName, double quantity, double price, long purchasedAt) {
        this.userId = userId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.price = price;
        this.purchasedAt = purchasedAt;
    }
}

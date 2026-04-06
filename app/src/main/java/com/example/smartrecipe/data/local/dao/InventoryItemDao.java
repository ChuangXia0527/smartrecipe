package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.InventoryItem;

import java.util.List;

@Dao
public interface InventoryItemDao {

    @Insert
    long insert(InventoryItem item);

    @Query("SELECT * FROM inventory_item WHERE userId = :userId ORDER BY updatedAt DESC")
    List<InventoryItem> listByUser(long userId);

    @Query("UPDATE inventory_item SET quantity = :quantity, updatedAt = :updatedAt WHERE id = :id")
    int updateQuantity(long id, double quantity, long updatedAt);
}

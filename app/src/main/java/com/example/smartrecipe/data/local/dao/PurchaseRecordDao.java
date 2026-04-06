package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.PurchaseRecord;

import java.util.List;

@Dao
public interface PurchaseRecordDao {

    @Insert
    long insert(PurchaseRecord record);

    @Query("SELECT * FROM purchase_record WHERE userId = :userId ORDER BY purchasedAt DESC LIMIT :limit")
    List<PurchaseRecord> recentByUser(long userId, int limit);

    @Query("SELECT * FROM purchase_record WHERE userId = :userId")
    List<PurchaseRecord> allByUser(long userId);

    @Query("SELECT * FROM purchase_record ORDER BY purchasedAt DESC")
    List<PurchaseRecord> allRecords();
}

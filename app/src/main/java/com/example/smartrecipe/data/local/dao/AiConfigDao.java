package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.AiConfig;

@Dao
public interface AiConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AiConfig aiConfig);

    @Query("SELECT * FROM ai_config WHERE id = 1 LIMIT 1")
    AiConfig getMainConfig();
}

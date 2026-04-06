package com.example.smartrecipe.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartrecipe.data.local.entity.SystemAnnouncement;

import java.util.List;

@Dao
public interface SystemAnnouncementDao {
    @Insert
    long insert(SystemAnnouncement announcement);

    @Query("SELECT * FROM system_announcement ORDER BY createdAt DESC")
    List<SystemAnnouncement> listAll();
}

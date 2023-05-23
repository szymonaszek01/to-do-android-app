package com.app.toDo.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.toDo.entity.Notification;

import java.util.List;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notification")
    List<Notification> findAll();

    @Insert()
    void insertNotification(Notification notification);

    @Update()
    void updateNotification(Notification notification);

    @Delete()
    void deleteNotification(Notification notification);
}

package com.app.toDo.configuration;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.app.toDo.dao.CategoryDao;
import com.app.toDo.dao.NotificationDao;
import com.app.toDo.dao.TaskDao;
import com.app.toDo.entity.Category;
import com.app.toDo.entity.Notification;
import com.app.toDo.entity.Task;
import com.app.toDo.util.ConstantUtil;

@Database(entities = {Task.class, Category.class, Notification.class}, exportSchema = false, version = 3)
public abstract class DatabaseConfiguration extends RoomDatabase {

    private static DatabaseConfiguration databaseConfiguration;

    public static synchronized DatabaseConfiguration getInstance(Context context) {
        if (databaseConfiguration == null) {
            databaseConfiguration = Room.databaseBuilder(context, DatabaseConfiguration.class, ConstantUtil.DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return databaseConfiguration;
    }

    public abstract TaskDao taskDao();

    public abstract CategoryDao categoryDao();

    public abstract NotificationDao notificationDao();
}

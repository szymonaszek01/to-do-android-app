package com.app.toDo.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.toDo.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM task")
    List<Task> findAll();

    @Insert()
    void insertTask(Task task);

    @Update()
    void updateTask(Task task);

    @Delete()
    void deleteTask(Task task);
}

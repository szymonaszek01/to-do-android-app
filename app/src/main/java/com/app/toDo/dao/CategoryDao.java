package com.app.toDo.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.toDo.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM category")
    List<Category> findAll();

    @Insert()
    void insertCategory(Category category);

    @Update()
    void updateCategory(Category category);

    @Delete()
    void deleteCategory(Category category);
}

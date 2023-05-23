package com.app.toDo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.app.toDo.configuration.DatabaseConfiguration;
import com.app.toDo.entity.Category;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    DatabaseConfiguration databaseConfiguration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseConfiguration = DatabaseConfiguration.getInstance(getApplicationContext());
        Category category = Category.builder().name("Sport").id(3).build();
        Executors.newSingleThreadExecutor().execute(() -> databaseConfiguration.categoryDao().insertCategory(category));
    }
}
package com.app.toDo.service;

import com.app.toDo.dao.CategoryDao;
import com.app.toDo.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private CategoryDao categoryDao;

    public List<Category> getCategoryList() {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            List<Category> categoryList = new ArrayList<>();

            executorService.execute(() -> {
                categoryList.addAll(categoryDao.findAll());
                countDownLatch.countDown();
            });
            countDownLatch.await();
            return categoryList;

        } catch (InterruptedException e) {
            return new ArrayList<>();
        }
    }

    public String getCategoryNameById(long id) {
        return getCategoryList()
                .stream()
                .filter(category -> category.getId() == id)
                .findFirst()
                .map(Category::getName)
                .orElse("");
    }

    public void addCategory(Category category) {
        executorService.execute(() -> categoryDao.insertCategory(category));
    }

    public void editCategory(Category category) {
        executorService.execute(() -> categoryDao.updateCategory(category));
    }

    public void deleteCategory(Category category) {
        executorService.execute(() -> categoryDao.deleteCategory(category));
    }
}

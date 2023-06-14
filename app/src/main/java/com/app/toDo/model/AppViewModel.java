package com.app.toDo.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.toDo.entity.Category;
import com.app.toDo.entity.Task;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class AppViewModel extends ViewModel {

    private final ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());

    private final MutableLiveData<Task> selectedTask = new MutableLiveData<>(getDefaultTask());

    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Category>> categoryList = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> filteringCategory = new MutableLiveData<>("");

    private final MutableLiveData<Integer> notificationTimeInSeconds = new MutableLiveData<>(60 * 60);

    public LiveData<Task> getSelectedTask() {
        return this.selectedTask;
    }

    public void setSelectedTask(Task selectedTask) {
        this.selectedTask.setValue(selectedTask);
    }

    public LiveData<List<Task>> getTaskList() {
        return this.taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList.setValue(taskList);
    }

    public LiveData<List<Category>> getCategoryList() {
        return this.categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList.setValue(categoryList);
    }

    public LiveData<String> getFilteringCategory() {
        return filteringCategory;
    }

    public void setFilteringCategory(String filteringCategory) {
        this.filteringCategory.setValue(filteringCategory);
    }

    public LiveData<Integer> getNotificationTimeInSeconds() {
        return notificationTimeInSeconds;
    }

    public void setNotificationTimeInSeconds(int notificationTimeInSeconds) {
        this.notificationTimeInSeconds.setValue(notificationTimeInSeconds);
    }

    public Category getDefaultCategory() {
        return Category.builder().id(0L).name("").build();
    }

    public Task getDefaultTask() {
        return Task.builder()
                .id(0L)
                .title("")
                .desc("")
                .uri("")
                .creationDateTimeEpoch(LocalDateTime.now().toEpochSecond(zoneOffset))
                .execDateTimeEpoch(LocalDateTime.now().toEpochSecond(zoneOffset))
                .categoryId(0L)
                .notificationCounter(0L)
                .isDone(false)
                .isNotificationOn(true)
                .build();
    }
}

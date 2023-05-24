package com.app.toDo.service;

import com.app.toDo.dao.TaskDao;
import com.app.toDo.entity.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TaskDao taskDao;

    public List<Task> getTaskList() {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            List<Task> taskList = new ArrayList<>();

            executorService.execute(() -> {
                taskList.addAll(taskDao.findAll());
                countDownLatch.countDown();
            });
            countDownLatch.await();
            return taskList;

        } catch (InterruptedException e) {
            return new ArrayList<>();
        }
    }

    public Optional<Task> getTaskById(long taskId) {
        return getTaskList()
                .stream()
                .filter(task -> task.getId() == taskId)
                .findFirst();
    }

    public void addTask(Task task) {
        executorService.execute(() -> taskDao.insertTask(task));
    }

    public void editTask(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }
}

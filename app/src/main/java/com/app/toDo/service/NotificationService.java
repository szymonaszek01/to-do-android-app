package com.app.toDo.service;

import com.app.toDo.dao.NotificationDao;
import com.app.toDo.entity.Notification;

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

public class NotificationService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private NotificationDao notificationDao;

    public List<Notification> getNotificationList() {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            List<Notification> notificationList = new ArrayList<>();

            executorService.execute(() -> {
                notificationList.addAll(notificationDao.findAll());
                countDownLatch.countDown();
            });
            countDownLatch.await();
            return notificationList;

        } catch (InterruptedException e) {
            return new ArrayList<>();
        }
    }

    public void addNotification(Notification notification) {
        executorService.execute(() -> notificationDao.insertNotification(notification));
    }

    public void editNotification(Notification notification) {
        executorService.execute(() -> notificationDao.updateNotification(notification));
    }

    public void deleteNotification(Notification notification) {
        executorService.execute(() -> notificationDao.deleteNotification(notification));
    }
}

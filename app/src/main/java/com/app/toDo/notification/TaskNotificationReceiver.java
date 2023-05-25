package com.app.toDo.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationCounter = Integer.parseInt(intent.getStringExtra("notificationCounter"));

        TaskNotificationManager notificationManager = TaskNotificationManager.builder().context(context).build();
        notificationManager.sendTaskNotification(notificationCounter, title, message);
    }
}

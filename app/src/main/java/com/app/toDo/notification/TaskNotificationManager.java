package com.app.toDo.notification;

import static com.app.toDo.util.ConstantUtil.CHANNEL_DESCRIPTION;
import static com.app.toDo.util.ConstantUtil.CHANNEL_ID;
import static com.app.toDo.util.ConstantUtil.CHANNEL_NAME;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.toDo.R;
import com.app.toDo.entity.Notification;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
public class TaskNotificationManager {

    private final Context context;

    public void scheduleTaskNotification(Notification notification) {
        // Create an intent to broadcast the notification
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("title", notification.getTitle());
        intent.putExtra("message", notification.getMessage());
        intent.putExtra("notificationCounter", String.valueOf(notification.getCounter()));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notification.getCounter(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule the notification using the notificationId and notification time
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notification.getNotificationDateTimeEpoch() * 1000, pendingIntent);
        }
    }

    public void sendTaskNotification(int notificationId, String title, String message) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add_24)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Set the notification sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    public void cancelTaskNotification(Notification notification) {
        // Cancel the scheduled notification using AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notification.getCounter(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        // Cancel the displayed notification using NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notification.getCounter());
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}

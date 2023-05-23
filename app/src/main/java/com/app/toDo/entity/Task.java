package com.app.toDo.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity(tableName = "task")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;

    private String desc;

    private String uri;

    @ColumnInfo(name = "creation_date_time_epoch")
    private long creationDateTimeEpoch;

    @ColumnInfo(name = "exec_date_time_epoch")
    private long execDateTimeEpoch;

    @ColumnInfo(name = "category_id")
    private long category;

    @ColumnInfo(name = "notification_id")
    private long notificationId;

    @ColumnInfo(name = "is_done")
    private boolean isDone = false;

    @ColumnInfo(name = "is_notification_on")
    private boolean isNotificationOn = true;
}

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
@Entity(tableName = "notification")
public class Notification {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int counter;

    private String title;

    private String message;

    @ColumnInfo(name = "exec_date_time_epoch")
    private long execDateTimeEpoch;

    @ColumnInfo(name = "notification_date_time_epoch")
    private long notificationDateTimeEpoch;
}

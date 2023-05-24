package com.app.toDo.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateConverter {

    public static LocalDateTime epochToDateTime(Long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
    }

    public static LocalDate localDateTimeToDate(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }

    public static String getPrettyLocalDateTime(Long epoch) {
        LocalDateTime dateTime = epochToDateTime(epoch);
        String newYorkDateTimePattern = "dd.MM.yyyy HH:mm";
        DateTimeFormatter newYorkDateFormatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern);
        return newYorkDateFormatter.format(dateTime);
    }
}

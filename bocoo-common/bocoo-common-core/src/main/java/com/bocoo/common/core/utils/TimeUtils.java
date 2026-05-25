package com.bocoo.common.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class TimeUtils {

    public static final ZoneOffset UTC = ZoneOffset.UTC;

    public static final String UTC_DATE_PATTERN = "yyyy-MM-dd";

    public static final String UTC_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter UTC_DATE_FORMATTER = DateTimeFormatter.ofPattern(UTC_DATE_PATTERN);

    public static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(UTC_DATE_TIME_PATTERN);

    public static final DateTimeFormatter UTC_ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private TimeUtils() {
    }

    public static LocalDateTime utcNow() {
        return LocalDateTime.now(UTC);
    }

    public static String formatUtc(LocalDateTime dateTime) {
        return formatUtc(dateTime, UTC_DATE_TIME_FORMATTER);
    }

    public static String formatUtc(LocalDateTime dateTime, String pattern) {
        return formatUtc(dateTime, DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatUtc(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime == null ? null : formatter.format(dateTime);
    }

    public static String formatUtcIso(LocalDateTime dateTime) {
        return dateTime == null ? null : UTC_ISO_INSTANT_FORMATTER.format(dateTime.toInstant(UTC));
    }

    public static LocalDateTime parseUtc(String dateTime) {
        return parseUtc(dateTime, UTC_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseUtc(String dateTime, String pattern) {
        return parseUtc(dateTime, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseUtc(String dateTime, DateTimeFormatter formatter) {
        return dateTime == null || dateTime.isBlank() ? null : LocalDateTime.parse(dateTime, formatter);
    }

    public static LocalDateTime parseUtcIso(String dateTime) {
        return dateTime == null || dateTime.isBlank()
            ? null
            : LocalDateTime.ofInstant(Instant.from(UTC_ISO_INSTANT_FORMATTER.parse(dateTime)), UTC);
    }

    public static Date toUtcDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.toInstant(UTC));
    }

    public static LocalDateTime toUtcLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), UTC);
    }
}

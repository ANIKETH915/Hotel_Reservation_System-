package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtil {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DATE_SQL = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm:ss a");
    public static final DateTimeFormatter HEADER_DATE = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    private DateUtil() {
    }

    public static int nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public static String format(LocalDate date) {
        return date == null ? "-" : DATE.format(date);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_TIME.format(dateTime);
    }

    public static String formatTime(LocalTime time) {
        return TIME.format(time);
    }

    public static String formatHeaderDate(LocalDate date) {
        return HEADER_DATE.format(date);
    }
}

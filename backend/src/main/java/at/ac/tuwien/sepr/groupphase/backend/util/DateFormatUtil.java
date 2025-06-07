package at.ac.tuwien.sepr.groupphase.backend.util;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatting dates in a consistent, human-readable format.
 */
public class DateFormatUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private DateFormatUtil() {
        // Utility class - no instances
    }

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }
}
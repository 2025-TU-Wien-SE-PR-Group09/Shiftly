package at.ac.tuwien.sepr.groupphase.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This service provides methods to get the current date and time.
 * It is used to avoid using LocalDateTime.now() directly in the code,
 * which makes it easier to test and mock.
 */
public interface TimeService {
    /**
     * Returns the current date and time.
     *
     * @return the current date and time
     */
    LocalDateTime now();

    /**
     * Returns the current date.
     *
     * @return the current date
     */
    LocalDate currentDate();

    /**
     * Returns the next Monday in the current month.
     *
     * @return next Monday as LocalDate
     */
    LocalDate nextMondayInMonth(LocalDate month);

}
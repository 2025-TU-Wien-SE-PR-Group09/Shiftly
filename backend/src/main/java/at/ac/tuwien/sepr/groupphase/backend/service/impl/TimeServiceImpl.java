package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;


@Service
public class TimeServiceImpl implements TimeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public LocalDateTime now() {
        LOGGER.trace("now()");
        return LocalDateTime.now();
    }

    @Override
    public LocalDate currentDate() {
        LOGGER.trace("currentDate()");
        return LocalDate.now();
    }

    @Override
    public LocalDate nextMondayInMonth(LocalDate month) {
        LOGGER.trace("nextMondayInMonth({})", month);
        if(month.getDayOfWeek() == DayOfWeek.MONDAY) {
            return month;
        }

        return month.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }
}

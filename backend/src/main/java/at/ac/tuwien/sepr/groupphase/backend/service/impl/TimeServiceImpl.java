package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;


@Service
public class TimeServiceImpl implements TimeService {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDate currentDate() {
        return LocalDate.now();
    }

    @Override
    public LocalDate nextMondayInMonth(LocalDate month) {
        return month.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }
}

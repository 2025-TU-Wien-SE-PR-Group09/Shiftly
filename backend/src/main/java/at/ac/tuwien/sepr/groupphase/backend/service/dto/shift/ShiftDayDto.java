package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public record ShiftDayDto(DayOfWeek day, LocalTime startTime, Duration duration) {
}

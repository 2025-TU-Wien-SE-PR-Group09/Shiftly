package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

public record CreateShiftDayDto(Optional<DayOfWeek> day, Optional<LocalTime> startTime, Optional<Duration> duration) {
}

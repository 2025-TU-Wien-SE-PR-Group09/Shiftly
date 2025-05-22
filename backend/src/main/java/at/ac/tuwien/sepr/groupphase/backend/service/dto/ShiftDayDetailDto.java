package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public record ShiftDayDetailDto(
    DayOfWeek day,
    LocalTime startTime,
    Duration duration,
    List<String> assignedUsers
) {
}

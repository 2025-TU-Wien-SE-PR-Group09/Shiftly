package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public record ShiftDayResponseDto(DayOfWeek day, LocalTime startTime, Duration duration) {
}
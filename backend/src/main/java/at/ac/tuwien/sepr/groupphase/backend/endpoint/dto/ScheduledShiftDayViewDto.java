package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Scheduled shift day")
public record ScheduledShiftDayViewDto(
    @Schema(description = "Description of the shift", example = "Morning Shift")
    DayOfWeek day,
    @Schema(description = "Start time of the shift", example = "08:00")
    LocalTime startTime,
    @Schema(description = "Duration of the shift", example = "PT8H")
    Duration duration,
    @Schema(description = "List of assigned user emails")
    List<String> assignedUserEmails
) {
}
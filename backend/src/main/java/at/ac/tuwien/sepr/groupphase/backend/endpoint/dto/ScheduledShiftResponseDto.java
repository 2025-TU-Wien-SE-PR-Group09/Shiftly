package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Scheduled shift response")
public record ScheduledShiftResponseDto(
    @Schema(description = "Calendar week of the shift", example = "42")
    int calendarWeek,
    @Schema(description = "Year of the shift", example = "2023")
    int year,
    @Schema(description = "Description of the shift", example = "Morning Shift")
    String shiftDescription,
    @Schema(description = "Start date of the week", example = "2023-10-16")
    LocalDate weekStartDate
) {
}


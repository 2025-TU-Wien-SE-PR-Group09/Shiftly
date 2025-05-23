package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Schema(description = "Shift day response")
public record ShiftDayResponseDto(
    @Schema(description = "Day of the week", example = "MONDAY")
    DayOfWeek day,
    @Schema(description = "Start time of the shift", example = "08:00")
    LocalTime startTime,
    @Schema(description = "Duration of the shift", example = "PT8H")
    Duration duration) {
}
package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Detailed view of a scheduled shift")
public record ScheduledShiftDetailedViewDto(
    @Schema(description = "Description of the shift", example = "Morning Shift")
    String shiftDescription,
    @Schema(description = "Calendar week of the shift", example = "42")
    LocalDate weekStartDate,
    @Schema(description = "List of scheduled shift days")
    List<ScheduledShiftDayViewDto> days
) {
}
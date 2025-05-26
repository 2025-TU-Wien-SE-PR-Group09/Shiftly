package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Response for shift plan blueprint")
public record PlanBlueprintResponse(
    @Schema(description = "ID of the plan blueprint", example = "1")
    Long id,
    @Schema(description = "Description of the plan blueprint", example = "Weekly production plan")
    String description,
    @Schema(description = "Name of  department", example = "Production")
    String department,
    @Schema(description = "List of shifts")
    List<ShiftResponseDto> shifts) {

    @Schema(description = "Response for shift plan blueprint")
    public record ShiftResponseDto(
        @Schema(description = "Unique identifier of the shift", example = "1")
        Long id,
        @Schema(description = "Name of the department", example = "Production")
        String description,
        @Schema(description = "Number of employees needed for the shift", example = "5")
        int manPower,
        @Schema(description = "List of shift shiftWeeks")
        List<ShiftWeekResponseDto> shiftWeeks) {
    }

    @Schema(description = "Response for a week of shifts")
    public record ShiftWeekResponseDto(
        @Schema(description = "List of shift day blueprints")
        List<ShiftDayResponseDto> shiftDays) {
    }

    @Schema(description = "Shift day response")
    public record ShiftDayResponseDto(
        @Schema(description = "Day of the week", example = "MONDAY")
        DayOfWeek day,
        @Schema(description = "Start time of the shift", example = "08:00")
        LocalTime startTime,
        @Schema(description = "Duration of the shift", example = "PT8H")
        Duration duration) {
    }

}
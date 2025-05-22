package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response for a week of shifts")
public record ShiftWeekResponseDto(
    @Schema(description = "List of shift day blueprints")
    List<ShiftDayResponseDto> shiftDays) {
}
package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response for shift plan blueprint")
public record ShiftResponseDto(
    @Schema(description = "Name of the department", example = "Production")
    String description,
    @Schema(description = "Number of employees needed for the shift", example = "5")
    int manPower,
    @Schema(description = "List of shift weeks")
    List<ShiftWeekResponseDto> shiftWeeks) {
}
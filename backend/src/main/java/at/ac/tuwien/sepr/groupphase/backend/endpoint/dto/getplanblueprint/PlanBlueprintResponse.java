package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response for shift plan blueprint")
public record PlanBlueprintResponse(
    @Schema(description = "Name of  department", example = "Production")
    String department,
    @Schema(description = "List of shifts")
    List<ShiftResponseDto> shifts) {

}
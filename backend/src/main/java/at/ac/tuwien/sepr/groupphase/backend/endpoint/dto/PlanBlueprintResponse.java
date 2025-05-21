package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record PlanBlueprintResponse(String department, List<ShiftResponseDto> shifts) {

}
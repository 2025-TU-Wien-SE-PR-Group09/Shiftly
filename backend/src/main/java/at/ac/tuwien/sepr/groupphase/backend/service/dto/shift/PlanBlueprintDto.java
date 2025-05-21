package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;


import java.util.List;

public record PlanBlueprintDto(String department, List<ShiftDto> shifts) {
}
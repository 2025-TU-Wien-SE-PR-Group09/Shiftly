package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;


import java.util.List;

public record PlanBlueprintDto(Long id, String description, String department, List<ShiftBlueprintDto> shifts) {
}
package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;

import java.util.List;

public record DepartmentDto(Long id, String name, List<PlanBlueprintDto> plans) {
}
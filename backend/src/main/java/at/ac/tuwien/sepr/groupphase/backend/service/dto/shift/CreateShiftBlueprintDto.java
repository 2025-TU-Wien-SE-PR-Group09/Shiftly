package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

public record CreateShiftBlueprintDto(Long departmentId, String description, int manPower) {
}
package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

public record CreateShiftDto(Long departmentId, String description, int manPower) {
}
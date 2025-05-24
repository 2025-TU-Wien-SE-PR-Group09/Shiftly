package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.EmployeeDto;

public record EmployeeRestResponseDto(String email, String departmentName) {
}

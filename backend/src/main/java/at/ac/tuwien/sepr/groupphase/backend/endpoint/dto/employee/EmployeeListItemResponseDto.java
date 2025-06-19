package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.employee;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.EmployeeListItemDto;

public record EmployeeListItemResponseDto(String email, String firstName, String lastName, String role) {
    public static EmployeeListItemResponseDto from(EmployeeListItemDto dto) {
        return new EmployeeListItemResponseDto(dto.email(), dto.firstName(), dto.lastName(), dto.role());
    }
}

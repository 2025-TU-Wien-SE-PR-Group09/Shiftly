package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.EmployeeListItemDto;

public record EmployeeListItemResponseDto(String email) {
    public static EmployeeListItemResponseDto from(EmployeeListItemDto dto) {
        return new EmployeeListItemResponseDto(dto.email());
    }
}

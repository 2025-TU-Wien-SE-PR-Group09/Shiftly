package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record ShiftResponseDto(String description, int manPower, List<ShiftWeekResponseDto> shiftWeeks) {
}
package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.util.List;

public record ShiftDto(Long id, String description, int manPower, List<ShiftWeekDto> shiftWeeks) {
}
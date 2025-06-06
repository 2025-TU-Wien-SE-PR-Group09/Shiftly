package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.time.LocalDate;
import java.util.List;

public record ScheduledShiftDetailDto(
    String shiftDescription,
    LocalDate weekStart,
    List<ShiftDayDetailDto> days
) {
}

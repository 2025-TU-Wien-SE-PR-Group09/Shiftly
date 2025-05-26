package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public record PlanBlueprintCreationDto(
    String description,
    List<ShiftBlueprintCreationDto> shifts,
    Long departmentId
) {

    public record ShiftBlueprintCreationDto(String description, int manpower,
                                            List<ShiftWeekBlueprintCreationDto> weeks) {
    }

    public record ShiftWeekBlueprintCreationDto(List<ShiftDayBlueprintCreationDto> days) {
    }

    public record ShiftDayBlueprintCreationDto(
        Optional<DayOfWeek> dayOfWeek,
        Optional<LocalTime> startTime,
        Optional<Duration> duration
    ) {
    }
}

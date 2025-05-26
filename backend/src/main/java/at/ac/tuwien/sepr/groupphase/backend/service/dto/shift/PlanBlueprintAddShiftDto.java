package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public record PlanBlueprintAddShiftDto(Long planId, List<ShiftBlueprintAddToPlanDto> shifts) {

    public record ShiftBlueprintAddToPlanDto(String description, int manPower,
                                             List<ShiftWeekBlueprintAddToPlanDto> weeks) {
    }

    public record ShiftWeekBlueprintAddToPlanDto(List<ShiftDayBlueprintAddToPlanDto> days) {
    }

    public record ShiftDayBlueprintAddToPlanDto(
        Optional<DayOfWeek> dayOfWeek,
        Optional<LocalTime> startTime,
        Optional<Duration> duration
    ) {
    }
}





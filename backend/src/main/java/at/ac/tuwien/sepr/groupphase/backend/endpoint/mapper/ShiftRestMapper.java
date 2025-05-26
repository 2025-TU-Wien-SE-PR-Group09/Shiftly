package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AddShiftToPlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreatePlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GenerateConcretePlanDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.service.util.OptionalExtension.ofThrowable;

public class ShiftRestMapper {


    public static ConcretePlanGenerateDto mapFromRequest(Long planId, GenerateConcretePlanDto concretePlanGenerateDto) {
        return new ConcretePlanGenerateDto(
            planId,
            ofThrowable(concretePlanGenerateDto::startDate)
        );
    }

    public static PlanBlueprintAddShiftDto mapFromRequest(AddShiftToPlanBlueprintDto addShiftToPlanBlueprintDto) {
        return new PlanBlueprintAddShiftDto(
            addShiftToPlanBlueprintDto.planId(),
            addShiftToPlanBlueprintDto.shifts().stream()
                .map(shift -> new PlanBlueprintAddShiftDto.ShiftBlueprintAddToPlanDto(
                    shift.description(),
                    shift.manPower(),
                    shift.shiftWeeks().stream()
                        .map(week -> new PlanBlueprintAddShiftDto.ShiftWeekBlueprintAddToPlanDto(
                            week.shiftDays().stream()
                                .map(day -> new PlanBlueprintAddShiftDto.ShiftDayBlueprintAddToPlanDto(
                                    ofThrowable(() -> DayOfWeek.valueOf(day.day())),
                                    ofThrowable(() -> LocalTime.parse(day.startTime())),
                                    mapToDuration(day.startTime(), day.endTime())
                                )).toList()
                        )).toList()
                )).toList()
        );
    }

    public static PlanBlueprintCreationDto mapFromRequest(Long departmentId, CreatePlanBlueprintDto createPlanBlueprintDto) {
        return new PlanBlueprintCreationDto(
            createPlanBlueprintDto.description(),
            createPlanBlueprintDto.shifts().stream()
                .map(shift -> new PlanBlueprintCreationDto.ShiftBlueprintCreationDto(
                    shift.description(),
                    shift.manPower(),
                    shift.shiftWeeks().stream()
                        .map(week -> new PlanBlueprintCreationDto.ShiftWeekBlueprintCreationDto(
                            week.shiftDays().stream()
                                .map(day -> new PlanBlueprintCreationDto.ShiftDayBlueprintCreationDto(
                                    ofThrowable(() -> DayOfWeek.valueOf(day.day())),
                                    ofThrowable(() -> LocalTime.parse(day.startTime())),
                                    mapToDuration(day.startTime(), day.endTime())
                                )).toList()
                        )).toList()
                )).toList(),
            departmentId
        );
    }

    private static Optional<Duration> mapToDuration(String startTime, String endTime) {
        var startTimeOpt = ofThrowable(() -> LocalTime.parse(startTime));
        var endTimeOpt = ofThrowable(() -> LocalTime.parse(endTime));

        return startTimeOpt.flatMap(start -> endTimeOpt.map(end -> {
            if (end.isBefore(start) || end.equals(start)) {
                var startToMidnight = Duration.between(start, LocalTime.of(23, 59)).plus(Duration.ofMinutes(1));
                var midnightToEnd = Duration.between(LocalTime.of(0, 0), end);
                return startToMidnight.plus(midnightToEnd);
            }
            return Duration.between(start, end);
        }));
    }
}

package at.ac.tuwien.sepr.groupphase.backend.service.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint.ShiftCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint.ShiftDayCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint.PlanBlueprintResponse;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint.ShiftResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint.ShiftWeekResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint.ShiftDayResponseDto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.service.util.OptionalExtension.ofThrowable;

/**
 * Mapper class for the Shift Planning.
 */
public class ShiftPlanningMapper {

    /**
     * Mapper class for all classes related to Plan.
     */
    public static class Plans {

        /**
         * Converts a PlanBlueprintDto(Service DTO) to a PlanBlueprintResponse(Rest).
         *
         * @param planBlueprintDto the PlanBlueprintDto
         * @return the PlanBlueprintResponse(Rest)
         */
        public static PlanBlueprintResponse toResponse(PlanBlueprintDto planBlueprintDto) {
            return new PlanBlueprintResponse(planBlueprintDto.department(),
                planBlueprintDto.shifts().stream().map(ShiftPlanningMapper.Shifts::toResponse).toList());
        }

        /**
         * Converts a PlanBlueprint(DAO) entity to a PlanBlueprintDto(Rest).
         *
         * @param plan the PlanBlueprint entity
         * @return the PlanBlueprintDto
         */
        public static PlanBlueprintDto fromEntity(PlanBlueprint plan) {
            return new PlanBlueprintDto(plan.getDepartment().getName(),
                plan.getShifts().stream()
                    .map(ShiftPlanningMapper.Shifts::fromEntity)
                    .toList());
        }
    }

    /**
     * Mapper class for all classes related to Shift.
     */
    public static class Shifts {

        public static CreateShiftBlueprintDto toDto(Long departmentId, ShiftCreateDto shiftCreateDto) {
            return new CreateShiftBlueprintDto(departmentId, shiftCreateDto.getDescription(), shiftCreateDto.getManPower());
        }

        /**
         * Converts a Shift(DAO) entity to a ShiftDto(Service DTO).
         *
         * @param shiftBlueprint the Shift entity
         * @return the ShiftDto
         */
        public static ShiftBlueprintDto fromEntity(ShiftBlueprint shiftBlueprint) {
            var shiftWeeks = shiftBlueprint.getShiftWeeks().stream()
                .map(ShiftPlanningMapper.ShiftWeeks::fromEntity)
                .toList();

            return new ShiftBlueprintDto(shiftBlueprint.getId(), shiftBlueprint.getDescription(), shiftBlueprint.getManPower(), shiftWeeks);
        }

        /**
         * Converts a ShiftDto(Service DTO) to a ShiftResponseDto(Rest).
         *
         * @param shift the ShiftDto
         * @return the ShiftResponseDto(Rest)
         */
        public static ShiftResponseDto toResponse(ShiftBlueprintDto shift) {
            return new ShiftResponseDto(shift.description(), shift.manPower(),
                shift.shiftWeeks().stream().map(ShiftPlanningMapper.ShiftWeeks::toResponse).toList());
        }
    }

    /**
     * Mapper class for all classes related to ShiftWeek.
     */
    public static class ShiftWeeks {


        /**
         * Converts a ShiftWeek(DAO) entity to a ShiftWeekDto(Service DTO).
         *
         * @param shiftWeekBlueprint the ShiftWeek entity
         * @return the ShiftWeekDto
         */
        public static ShiftWeekBlueprintDto fromEntity(ShiftWeekBlueprint shiftWeekBlueprint) {
            var shiftDays = shiftWeekBlueprint.getDays().stream()
                .map(ShiftPlanningMapper.ShiftDays::fromEntity)
                .toList();
            return new ShiftWeekBlueprintDto(shiftDays);
        }

        /**
         * Converts a ShiftWeekDto(Service DTO) to a ShiftWeekResponseDto(Rest).
         *
         * @param shiftWeek the ShiftWeekDto
         * @return the ShiftWeekResponseDto(Rest)
         */
        public static ShiftWeekResponseDto toResponse(ShiftWeekBlueprintDto shiftWeek) {
            return new ShiftWeekResponseDto(
                shiftWeek.shiftDays().stream().map(ShiftPlanningMapper.ShiftDays::toResponse).toList());
        }
    }

    /**
     * Mapper class for all classes related to ShiftDay.
     */
    public static class ShiftDays {

        public static CreateShiftDayDto toDto(ShiftDayCreateDto shiftDayCreateDto) {

            var dayOfWeek = ofThrowable(() -> DayOfWeek.valueOf(shiftDayCreateDto.getDay()));
            var startTime = ofThrowable(() -> LocalTime.parse(shiftDayCreateDto.getStartTime()));
            var endTime = ofThrowable(() -> LocalTime.parse(shiftDayCreateDto.getEndTime()));

            Optional<Duration> duration = startTime.flatMap(st -> endTime.map(et -> Duration.between(st, et)));

            return new CreateShiftDayDto(
                dayOfWeek,
                startTime,
                duration
            );
        }

        /**
         * Converts a ShiftDay(DAO) entity to a ShiftDayDto(Service DTO).
         *
         * @param shiftDayBlueprint the ShiftDay entity
         * @return the ShiftDayDto
         */
        public static ShiftDayDto fromEntity(ShiftDayBlueprint shiftDayBlueprint) {
            return new ShiftDayDto(shiftDayBlueprint.getDay(), shiftDayBlueprint.getStartTime(), shiftDayBlueprint.getDuration());
        }

        /**
         * Converts a ShiftDayDto(Service DTO) to a ShiftDayResponseDto(Rest).
         *
         * @param shiftDayDto the ShiftDayDto
         * @return the ShiftDayResponseDto(Rest)
         */
        public static ShiftDayResponseDto toResponse(ShiftDayDto shiftDayDto) {
            return new ShiftDayResponseDto(shiftDayDto.day(), shiftDayDto.startTime(), shiftDayDto.duration());
        }
    }

}
package at.ac.tuwien.sepr.groupphase.backend.service.mapper;

import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.Shift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDay;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeek;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PlanBlueprintResponse;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShiftResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShiftWeekResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShiftDayResponseDto;

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

        /**
         * Converts a Shift(DAO) entity to a ShiftDto(Service DTO).
         *
         * @param shift the Shift entity
         * @return the ShiftDto
         */
        public static ShiftDto fromEntity(Shift shift) {
            var shiftWeeks = shift.getShiftWeeks().stream()
                .map(ShiftPlanningMapper.ShiftWeeks::fromEntity)
                .toList();

            return new ShiftDto(shift.getId(), shift.getDescription(), shift.getManPower(), shiftWeeks);
        }

        /**
         * Converts a ShiftDto(Service DTO) to a ShiftResponseDto(Rest).
         *
         * @param shift the ShiftDto
         * @return the ShiftResponseDto(Rest)
         */
        public static ShiftResponseDto toResponse(ShiftDto shift) {
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
         * @param shiftWeek the ShiftWeek entity
         * @return the ShiftWeekDto
         */
        public static ShiftWeekDto fromEntity(ShiftWeek shiftWeek) {
            var shiftDays = shiftWeek.getDays().stream()
                .map(ShiftPlanningMapper.ShiftDays::fromEntity)
                .toList();
            return new ShiftWeekDto(shiftDays);
        }

        /**
         * Converts a ShiftWeekDto(Service DTO) to a ShiftWeekResponseDto(Rest).
         *
         * @param shiftWeek the ShiftWeekDto
         * @return the ShiftWeekResponseDto(Rest)
         */
        public static ShiftWeekResponseDto toResponse(ShiftWeekDto shiftWeek) {
            return new ShiftWeekResponseDto(
                shiftWeek.shiftDays().stream().map(ShiftPlanningMapper.ShiftDays::toResponse).toList());
        }
    }

    /**
     * Mapper class for all classes related to ShiftDay.
     */
    public static class ShiftDays {

        /**
         * Converts a ShiftDay(DAO) entity to a ShiftDayDto(Service DTO).
         *
         * @param shiftDay the ShiftDay entity
         * @return the ShiftDayDto
         */
        public static ShiftDayDto fromEntity(ShiftDay shiftDay) {
            return new ShiftDayDto(shiftDay.getDay(), shiftDay.getStartTime(), shiftDay.getDuration());
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
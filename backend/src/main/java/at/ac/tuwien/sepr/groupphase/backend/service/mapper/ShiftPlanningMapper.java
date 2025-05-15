package at.ac.tuwien.sepr.groupphase.backend.service.mapper;

import at.ac.tuwien.sepr.groupphase.backend.entity.Plan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Shift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDay;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeek;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekDto;

public class ShiftPlanningMapper {

    public static PlanDto fromEntity(Plan plan) {
        return new PlanDto(plan.getDepartment().getName(),
            plan.getShifts().stream()
                .map(ShiftPlanningMapper::fromEntity)
                .toList());
    }

    public static ShiftDto fromEntity(Shift shift) {
        var shiftWeeks = shift.getShiftWeeks().stream()
            .map(ShiftPlanningMapper::fromEntity)
            .toList();

        return new ShiftDto(shift.getDescription(), shift.getManPower(), shiftWeeks);
    }

    public static ShiftWeekDto fromEntity(ShiftWeek shiftWeek) {
        var shiftDays = shiftWeek.getDays().stream()
            .map(ShiftPlanningMapper::fromEntity)
            .toList();
        return new ShiftWeekDto(shiftDays);
    }

    public static ShiftDayDto fromEntity(ShiftDay shiftDay) {
        return new ShiftDayDto(shiftDay.getDay(), shiftDay.getStartTime(), shiftDay.getDuration());
    }
}

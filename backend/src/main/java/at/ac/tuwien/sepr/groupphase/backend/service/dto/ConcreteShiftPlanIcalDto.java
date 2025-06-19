package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;

import java.util.List;

public record ConcreteShiftPlanIcalDto(
    List<ScheduledShift> scheduledShifts
) {
}

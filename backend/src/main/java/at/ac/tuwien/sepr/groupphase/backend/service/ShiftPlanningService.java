package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.CreateShiftDto;

import java.util.List;

/**
 * ShiftPlanningService is an interface that defines methods for creating and managing shifts.
 */
public interface ShiftPlanningService {

    /**
     * Creates a new shift based on the provided CreateShiftDto.
     *
     * @param createShiftDto the DTO containing the details of the shift to be created
     * @return the created ShiftDto
     */
    ShiftDto createShift(CreateShiftDto createShiftDto);

    /**
     * Adds a week to an existing shift.
     *
     * @param shiftId   the ID of the shift to which the week will be added
     * @param shiftWeek the DTO containing the details of the week to be added
     * @return the updated ShiftDto
     */
    ShiftDto addWeekToShift(Long shiftId, ShiftWeekDto shiftWeek);

    /**
     * Creates a new plan blueprint for a department based on the provided department ID and list of shift IDs.
     *
     * @param departmentId the ID of the department for which the plan will be created
     * @param shiftIds     the list of shift IDs to be included in the plan
     * @return the created PlanBlueprintDto
     */
    PlanBlueprintDto createPlan(Long departmentId, List<Long> shiftIds);
}
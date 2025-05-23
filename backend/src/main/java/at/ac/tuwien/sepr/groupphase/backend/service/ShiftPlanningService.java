package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.CreateShiftBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ScheduledShiftDetailDto;

import java.util.List;

/**
 * ShiftPlanningService is an interface that defines methods for creating and managing shifts.
 */
public interface ShiftPlanningService {

    /**
     * Creates a new shift-blueprint based on the provided CreateShiftDto.
     *
     * @param createShiftBlueprintDto the DTO containing the details of the shift to be created
     * @return the created ShiftDto
     */
    ShiftBlueprintDto createShiftBlueprint(CreateShiftBlueprintDto createShiftBlueprintDto);

    /**
     * Adds a week to an existing shift blueprint.
     *
     * @param shiftId    the ID of the shift to which the week will be added
     * @param shiftWeeks the DTO containing the details of the week to be added
     * @return the updated ShiftDto
     */
    ShiftBlueprintDto addWeeksToShift(Long shiftId, List<ShiftWeekBlueprintDto> shiftWeeks);

    /**
     * Creates a new plan blueprint for a department based on the provided department ID and list of shift IDs.
     *
     * @param departmentId the ID of the department for which the plan will be created
     * @param shiftIds     the list of shift IDs to be included in the plan
     * @return the created PlanBlueprintDto
     */
    PlanBlueprintDto createPlanBlueprint(Long departmentId, List<Long> shiftIds);

    PlanBlueprintDto addShiftToCurrentPlan(Long departmentId, ShiftBlueprintDto shift);


    /**
     * Generates a concrete plan for a department based on the provided department ID.
     * This is done quarterly, meaning that the plan will be generated for a specific quarter of the year.
     *
     * @param departmentId the ID of the department for which the plan will be generated
     * @return the generated ConcreteShiftPlan
     */
    ConcreteShiftPlan generateConcreteQuarterlyPlan(Long departmentId);

    /**
     * Retrieves a detailed view of the scheduled shifts for a department based on the concrete quarterly plan.
     *
     * @param departmentId the ID of the department for which the scheduled shifts will be retrieved
     * @return a list of ScheduledShiftDetailedViewDto containing the details of the scheduled shifts
     */
    List<ScheduledShiftDetailDto> getCurrentConcretePlan(Long departmentId);

}
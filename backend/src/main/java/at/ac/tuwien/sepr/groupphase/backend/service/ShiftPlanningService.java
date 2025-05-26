package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ScheduledShiftDetailDto;

import java.util.List;

/**
 * ShiftPlanningService is an interface that defines methods for creating and managing shifts.
 */
public interface ShiftPlanningService {

    PlanBlueprintDto createPlanBlueprint(PlanBlueprintCreationDto createPlanBlueprintDto);

    PlanBlueprintDto addShiftToPlan(PlanBlueprintAddShiftDto addShiftDto);

    /**
     * Generates a concrete plan for a department based on the provided department ID.
     * This is done quarterly, meaning that the plan will be generated for a specific quarter of the year.
     *
     * @return the generated ConcreteShiftPlan
     */
    ConcreteShiftPlan generateConcreteQuarterlyPlan(ConcretePlanGenerateDto concretePlanGenerateDto);

    /**
     * Retrieves a detailed view of the scheduled shifts for a department based on the concrete quarterly plan.
     *
     * @param departmentId the ID of the department for which the scheduled shifts will be retrieved
     * @return a list of ScheduledShiftDetailedViewDto containing the details of the scheduled shifts
     */
    ConcreteShiftPlan getCurrentConcretePlan(Long departmentId);

}
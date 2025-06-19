package at.ac.tuwien.sepr.groupphase.backend.service.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;

import java.util.List;
import java.util.Optional;


/**
 * ShiftPlanningService is an interface that defines methods for creating and managing shifts.
 */
public interface ShiftPlanningService {

    /**
     * Creates a new plan blueprint based on the provided PlanBlueprintCreationDto.
     *
     * @param createPlanBlueprintDto the DTO containing the details for creating a plan blueprint
     * @return the created PlanBlueprintDto
     */
    PlanBlueprintDto createPlanBlueprint(PlanBlueprintCreationDto createPlanBlueprintDto);

    /**
     * Adds a shift to an existing plan blueprint.
     *
     * @param addShiftDto the DTO containing the details of the shift to be added
     * @return the updated PlanBlueprintDto with the added shift
     */
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
     * @param departmentName the Name of the department for which the scheduled shifts will be retrieved
     * @return a list of ScheduledShiftDetailedViewDto containing the details of the scheduled shifts
     */
    ConcreteShiftPlan getCurrentConcretePlan(String departmentName);

    /**
     * Retrieves the department of a shift blueprint for a given ID.
     *
     * @param id the ID of the shift blueprint
     * @return an Optional containing the DepartmentNameDto if found, or empty if not found
     */
    Optional<DepartmentNameDto> getDeparmentNameForShiftBlueprint(Long id);

    /**
     * Retrieves all shift plans for a given department departmentName.
     *
     * @param departmentName the departmentName of the department for which to retrieve all shift plans
     * @return a ConcreteShiftPlan containing all plans for the specified department
     */
    List<ConcreteShiftPlan> getAllNotOverridenPlans(String departmentName);

    /**
     * Retrieves the current concrete plan for a department with eagerly loaded shifts and assignments.
     * This method prevents LazyInitializationException by using JOIN FETCH.
     *
     * @param departmentName the name of the department for which to retrieve the current concrete plan
     * @return the current ConcreteShiftPlan with all shifts and assignments loaded
     */
    ConcreteShiftPlan getCurrentConcretePlanWithShifts(String departmentName);
}
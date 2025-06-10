package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;

import java.util.List;
import java.util.Optional;

/**
 * Validator interface for ShiftWeek.
 * This interface defines a method to validate the working hours respecting the
 * laws and regulations.
 */
public interface ShiftPlanningValidator {
    /**
     * Validates the week.
     *
     * @param shiftWeekBlueprintDto the ShiftWeekDto to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */
    Optional<ValidationErrors> validateWeek(ShiftWeekBlueprintDto shiftWeekBlueprintDto);

    /**
     * Validates the day.
     *
     * @param shiftWeekBlueprintDto the ShiftWeekDto to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */
    Optional<ValidationErrors> validateDay(ShiftDayBlueprint shiftWeekBlueprintDto);

    /**
     * Validates the structure of shift days across a department.
     *  - no overlapping shift times are allowed per day
     *
     * @param shiftBlueprints all shift blueprints belonging to one department
     * @return an Optional containing ValidationErrors if any structural issues are found
     */
    Optional<ValidationErrors> validateDayStructuresPerDepartment(List<ShiftBlueprint> shiftBlueprints);

    /**
     * Validates the weekly durations of shifts per plan.
     *
     * @param shifts the list of ShiftBlueprints to validate
     * @param plan the PlanBlueprint containing the shifts
     * @return an Optional containing ValidationErrors if any issues are found, or an empty Optional if validation succeeds
     */
    Optional<ValidationErrors> validateWeeklyDurationsPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint plan);

    Optional<ValidationErrors> validateConsistentWeekCountPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint targetPlan);



}
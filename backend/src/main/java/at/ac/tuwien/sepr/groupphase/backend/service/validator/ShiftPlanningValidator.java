package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
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
     * Validates the whole plan.
     *
     * @param planBlueprint the PlanBlueprint to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */

    Optional<ValidationErrors> validatePlan(PlanBlueprint planBlueprint);

    /**
     * Validates the whole shift.
     *
     * @param shiftBlueprint the ShiftBlueprint to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */

    Optional<ValidationErrors> validateShift(ShiftBlueprint shiftBlueprint);

    /**
     * Validates the week.
     *
     * @param shiftWeekBlueprint the ShiftWeekBlueprint to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */
    Optional<ValidationErrors> validateWeek(ShiftWeekBlueprint shiftWeekBlueprint);

    /**
     * Validates the day.
     *
     * @param shiftWeekBlueprintDto the ShiftDayBlueprint to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */
    Optional<ValidationErrors> validateDay(ShiftDayBlueprint shiftWeekBlueprintDto);


    /**
     * Validates that no overlapping shifts exist within a plan.
     *
     *<p>
     * Shifts are grouped by plan, week, and day. Within each group, overlapping time intervals are detected.
     *
     * @param shifts the list of shift blueprints to check
     * @param plan   the plan blueprint these shifts belong to
     * @return an Optional containing validation errors if overlaps are detected, or empty if all shifts are non-overlapping
     */
    Optional<ValidationErrors> validateOverlappingShiftsPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint plan);


    /**
     * Validates that all shifts in the plan have equal weekly working durations.
     *
     *<p>
     * For each week index, this method ensures that the sum of durations across days is consistent across all shifts.
     *
     * @param shifts the list of shift blueprints to check
     * @param plan   the plan blueprint these shifts belong to
     * @return an Optional containing validation errors if inconsistencies are found, or empty if all durations match
     */
    Optional<ValidationErrors> validateWeeklyDurationsPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint plan);

    /**
     * Validates that all shifts within a plan cover the same number of weeks.
     *
     * <p>
     * Ensures that each shift includes the same set of week indices so that all weeks are consistently covered.
     *
     * @param shifts     the list of shift blueprints to validate
     * @param targetPlan the plan blueprint that the shifts belong to
     * @return an Optional containing validation errors if week coverage is inconsistent, or empty if all are aligned
     */
    Optional<ValidationErrors> validateConsistentWeekCountPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint targetPlan);

    /**
     * Validates that all provided ShiftBlueprints have non-empty descriptions.
     *
     *<p>
     *
     * @param shifts the list of ShiftBlueprints to validate
     * @return an Optional containing ValidationErrors if any descriptions are missing or blank,
     *         or an empty Optional if all descriptions are valid
     */
    Optional<ValidationErrors> validateDescriptions(List<ShiftBlueprint> shifts);

    /**
     * Validates that the manpower requirements for each shift are met.
     *
     * <p>
     * Checks that each shift has a positive manpower value and that it meets the minimum requirements.
     *
     * @param shifts the list of ShiftBlueprints to validate
     * @return an Optional containing ValidationErrors if any shifts have invalid manpower values,
     *         or an empty Optional if all shifts are valid
     */
    Optional<ValidationErrors> validateManpowerMinimum(List<ShiftBlueprint> shifts);




}
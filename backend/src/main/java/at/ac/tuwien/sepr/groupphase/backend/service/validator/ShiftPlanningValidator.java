package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import java.util.Optional;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AddShiftToPlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;

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
}
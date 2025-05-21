package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import java.util.Optional;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekDto;

/**
 * Validator interface for ShiftWeek.
 * This interface defines a method to validate the working hours respecting the
 * laws and regulations.
 */
public interface ShiftWeekValidator {
    /**
     * Validates the working hours of a ShiftWeekDto.
     *
     * @param shiftWeekDto the ShiftWeekDto to validate
     * @return an Optional containing ValidationErrors if validation fails, or an empty Optional if validation succeeds
     */
    Optional<ValidationErrors> validateWorkingHours(ShiftWeekDto shiftWeekDto);
}
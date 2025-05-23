package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import java.util.List;

/**
 * ValidationErrors is a record that holds a list of validation errors.
 * It provides a method to check if the validation is valid (i.e., if there are no errors).
 */
public record ValidationErrors(List<String> validationErrors) {

    public boolean isValid() {
        return validationErrors.isEmpty();
    }
}
package at.ac.tuwien.sepr.groupphase.backend.exception;

import at.ac.tuwien.sepr.groupphase.backend.service.validator.ValidationErrors;

import java.util.ArrayList;
import java.util.List;

public class ConflictException extends RuntimeException {
    private final List<String> errors = new ArrayList<>();

    public ConflictException(String message) {
        super(message);
        errors.add(message);
    }

    public ConflictException(ValidationErrors validationErrors) {
        super(String.join("; ", validationErrors.getValidationErrors()));
        errors.addAll(validationErrors.getValidationErrors());
    }

    public List<String> getErrors() {
        return errors;
    }
}

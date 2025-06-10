package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrors {

    private final List<String> validationErrors = new ArrayList<>();

    public ValidationErrors() {
    }

    public void add(String error) {
        this.validationErrors.add(error);
    }

    public void addAll(ValidationErrors other) {
        this.validationErrors.addAll(other.validationErrors);
    }

    public List<String> getValidationErrors() {
        return List.copyOf(validationErrors);
    }

    public boolean isValid() {
        return validationErrors.isEmpty();
    }

    public boolean isEmpty() {
        return validationErrors.isEmpty();
    }

    @Override
    public String toString() {
        return String.join(", ", validationErrors);
    }


}

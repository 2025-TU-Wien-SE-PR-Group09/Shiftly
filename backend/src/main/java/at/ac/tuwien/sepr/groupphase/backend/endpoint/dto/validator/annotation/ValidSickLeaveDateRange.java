package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.SickLeaveDateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SickLeaveDateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSickLeaveDateRange {
    String message() default "Sick leave end date must be after start date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

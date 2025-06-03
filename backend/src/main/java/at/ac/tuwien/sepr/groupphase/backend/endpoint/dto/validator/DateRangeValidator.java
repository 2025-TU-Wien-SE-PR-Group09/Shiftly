package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, VacationRequestRestDto> {
    @Override
    public boolean isValid(VacationRequestRestDto vacationRequestRestDto, jakarta.validation.ConstraintValidatorContext context) {
        if (vacationRequestRestDto == null) {
            return true;
        }
        if (vacationRequestRestDto.getStartDate() == null || vacationRequestRestDto.getEndDate() == null) {
            return false;
        }
        return !vacationRequestRestDto.getStartDate().isAfter(vacationRequestRestDto.getEndDate());
    }
}

package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SickLeaveCertificateUploadDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidSickLeaveDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SickLeaveDateRangeValidator implements ConstraintValidator<ValidSickLeaveDateRange, SickLeaveCertificateUploadDto> {

    @Override
    public boolean isValid(SickLeaveCertificateUploadDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getStartDate() == null || dto.getEndDate() == null) {
            return false;
        }
        return !dto.getStartDate().isAfter(dto.getEndDate());
    }
}

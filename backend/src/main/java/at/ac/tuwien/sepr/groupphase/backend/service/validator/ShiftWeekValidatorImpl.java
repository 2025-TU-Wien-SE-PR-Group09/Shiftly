package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShiftWeekValidatorImpl implements ShiftWeekValidator {
    @Override
    public Optional<ValidationErrors> validateWorkingHours(ShiftWeekBlueprintDto shiftWeekBlueprintDto) {
        return Optional.empty();
    }
}

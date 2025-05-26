package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShiftPlanningValidatorImpl implements ShiftPlanningValidator {

    @Override
    public Optional<ValidationErrors> validateWeek(ShiftWeekBlueprintDto shiftWeekBlueprintDto) {
        return Optional.empty();
    }

    @Override
    public Optional<ValidationErrors> validateDay(ShiftDayBlueprint shiftWeekBlueprintDto) {
        return Optional.empty();
    }
}

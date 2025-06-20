package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddShiftToPlanBlueprintDto(Long planId, @Size(min = 1) @Valid List<AddShiftBlueprintDto> shifts) {

    public record AddShiftBlueprintDto(@NotNull @NotBlank(message = "Blueprint name must not be blank") @Pattern(
        regexp = "^[A-ZÄÖÜa-zäöüß0-9 ,.!?()'\"-]*$",
        message = "The description contains illegal characters."
    ) String description, @Positive Integer manPower,
                                       @NotNull List<AddShiftWeekBlueprintDto> shiftWeeks) {
    }

    public record AddShiftWeekBlueprintDto(@NotNull List<ShiftDayBlueprintDto> shiftDays) {
    }

    public record ShiftDayBlueprintDto(
        @NotBlank String day,
        @NotBlank String startTime,
        @NotBlank String endTime
    ) {
    }
}

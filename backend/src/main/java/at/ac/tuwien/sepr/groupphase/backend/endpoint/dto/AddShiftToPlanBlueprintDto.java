package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddShiftToPlanBlueprintDto(Long planId, @Size(min = 1) List<AddShiftBlueprintDto> shifts) {

    public record AddShiftBlueprintDto(@NotBlank String description, @Positive Integer manPower,
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

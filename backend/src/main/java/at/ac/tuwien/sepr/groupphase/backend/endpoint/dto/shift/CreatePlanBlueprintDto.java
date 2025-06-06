package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePlanBlueprintDto(String description, @Size(min = 1) List<CreateShiftBlueprintDto> shifts) {

    public record CreateShiftBlueprintDto(@NotBlank String description, @Positive Integer manPower,
                                          @NotNull List<CreateShiftWeekBlueprintDto> shiftWeeks) {
    }

    public record CreateShiftWeekBlueprintDto(@NotNull List<CreateShiftDayBlueprintDto> shiftDays) {
    }

    public record CreateShiftDayBlueprintDto(
        @NotBlank String day,
        @NotBlank String startTime,
        @NotBlank String endTime
    ) {
    }

}

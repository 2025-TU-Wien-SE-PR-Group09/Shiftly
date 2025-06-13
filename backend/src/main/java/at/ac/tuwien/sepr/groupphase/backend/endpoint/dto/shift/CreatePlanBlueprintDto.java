package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePlanBlueprintDto(@NotBlank
                                     @Size(min = 3, max = 255, message = "Description must have a size between 3 and 255 characters.")
                                     @Pattern(
                                         regexp = "^[A-ZÄÖÜa-zäöüß0-9 ,.!?()'\"-]{3,255}$",
                                         message = "The description contains illegal characters."
                                     ) String description, List<CreateShiftBlueprintDto> shifts) {

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

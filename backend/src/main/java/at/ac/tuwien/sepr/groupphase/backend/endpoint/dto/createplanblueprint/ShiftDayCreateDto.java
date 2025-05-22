package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ShiftDayCreateDto {
    @NotBlank
    private String day; // e.g., "MONDAY"

    @Pattern(regexp = "^\\d{2}:\\d{2}$")
    private String startTime;

    @Pattern(regexp = "^\\d{2}:\\d{2}$")
    private String endTime;

    public String getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}

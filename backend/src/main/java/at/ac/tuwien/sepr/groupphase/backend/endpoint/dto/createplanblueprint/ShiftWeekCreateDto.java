package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ShiftWeekCreateDto {
    @NotNull
    private List<ShiftDayCreateDto> shiftDays;

    public List<ShiftDayCreateDto> getShiftDays() {
        return shiftDays;
    }
}

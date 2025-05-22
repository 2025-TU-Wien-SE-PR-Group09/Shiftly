package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreatePlanBlueprintDto {

    @NotNull
    private List<ShiftCreateDto> shifts;

    public List<ShiftCreateDto> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftCreateDto> shifts) {
        this.shifts = shifts;
    }
}

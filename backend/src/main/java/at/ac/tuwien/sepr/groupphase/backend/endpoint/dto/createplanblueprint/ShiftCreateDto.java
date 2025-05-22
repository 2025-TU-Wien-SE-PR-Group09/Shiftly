package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class ShiftCreateDto {
    @NotBlank
    private String description;

    @Positive
    private int manPower;

    @NotNull
    private List<ShiftWeekCreateDto> shiftWeeks;

    public List<ShiftWeekCreateDto> getShiftWeeks() {
        return shiftWeeks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public int getManPower() {
        return manPower;
    }
}

package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidSickLeaveDateRange;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO for uploading a sick leave certificate (excluding the file itself).
 */
@ValidSickLeaveDateRange
public class SickLeaveCertificateUploadDatesDto {

    @NotNull(message = "Start date must not be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    @FutureOrPresent(message = "End date must be in the present or future")
    private LocalDate endDate;


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "SickLeaveCertificateUploadDatesDto{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}

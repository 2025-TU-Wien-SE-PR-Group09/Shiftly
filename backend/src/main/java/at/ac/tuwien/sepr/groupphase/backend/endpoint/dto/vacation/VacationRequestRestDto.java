package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidDateRange;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@ValidDateRange
public class VacationRequestRestDto {

    @NotNull(message = "Start date must not be null")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;


    public VacationRequestRestDto() {
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the vacation request.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date of the vacation request.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the vacation request.
     *
     * @param endDate the end date to set
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    /**
     * Converts a VacationRequestRestDto to a VacationRequestDto.
     *
     * @param dto the VacationRequestRestDto to convert
     * @return the converted VacationRequestDto
     */
    public static VacationRequestRestDto from(VacationRequestDto dto) {
        VacationRequestRestDto restDto = new VacationRequestRestDto();
        restDto.setStartDate(dto.getStartDate());
        restDto.setEndDate(dto.getEndDate());
        return restDto;
    }

    @Override
    public String toString() {
        return "VacationRequestRestDto{" +
            "startDate=" + startDate +
            ", endDate=" + endDate +
            '}';
    }
}
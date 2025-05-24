package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidDateRange;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@ValidDateRange
public class VacationRequestRestDto {

    @NotNull(message = "Start date must not be null")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Size(min = 1, max = 20, message = "Status must be between 1 and 20 characters")
    private String status;


    public VacationRequestRestDto() {
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public static VacationRequestRestDto from(VacationRequestDto dto) {
        VacationRequestRestDto restDto = new VacationRequestRestDto();
        restDto.setStartDate(dto.getStartDate());
        restDto.setEndDate(dto.getEndDate());
        restDto.setStatus(dto.getStatus());
        return restDto;
    }


}
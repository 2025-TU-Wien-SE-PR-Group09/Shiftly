package at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidDateRange;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@ValidDateRange
public class VacationRequestDto {

    @Email
    @NotNull(message = "Email must not be null")
    private String employeeEmail;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private LocalDate endDate;




    public VacationRequestDto(String employeeEmail, LocalDate startDate, LocalDate endDate) {
        this.employeeEmail = employeeEmail;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }




    /**
     * Converts a VacationRequestDto to a VacationRequestResponseDto.
     *
     * @param restDto the status of the vacation request
     * @return a VacationRequestResponseDto
     */
    public static VacationRequestDto from(VacationRequestRestDto restDto) {
        return new VacationRequestDto(
            null,
            restDto.getStartDate(),
            restDto.getEndDate()
        );
    }

}

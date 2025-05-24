package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestRestDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class VacationRequestDto {
    private Long id;
    @Email
    @NotNull(message = "Email must not be null")
    private String employeeEmail;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private LocalDate endDate;

    @NotNull(message = "Status must not be null")
    private String status;


    public VacationRequestDto(Long id, String employeeEmail, LocalDate startDate, LocalDate endDate, String status) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }


    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static VacationRequestDto from(VacationRequestRestDto restDto) {
        return new VacationRequestDto(
            null,
            null,
            restDto.getStartDate(),
            restDto.getEndDate(),
            "PENDING"
        );
    }
}

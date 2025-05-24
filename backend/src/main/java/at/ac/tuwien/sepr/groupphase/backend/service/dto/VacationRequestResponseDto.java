package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import java.time.LocalDate;

public class VacationRequestResponseDto {

    private Long id;
    private String employeeEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public VacationRequestResponseDto(Long id, String employeeEmail, LocalDate startDate, LocalDate endDate, String status) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() {
        return id;
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
}

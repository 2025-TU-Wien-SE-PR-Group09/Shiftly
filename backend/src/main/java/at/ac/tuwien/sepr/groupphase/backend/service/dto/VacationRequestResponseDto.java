package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import java.time.LocalDate;

public class VacationRequestResponseDto {

    private Long id;
    private String employeeEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private VacationStatus status;

    public VacationRequestResponseDto(Long id, String employeeEmail, LocalDate startDate, LocalDate endDate, VacationStatus status) {
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

    public VacationStatus getStatus() {
        return status;
    }


}

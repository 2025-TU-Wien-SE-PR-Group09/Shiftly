package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;

import java.time.LocalDate;

public class VacationRequestResponseRestDto {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private VacationStatus status;
    private String employeeEmail;

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public VacationStatus getStatus() {
        return status;
    }

    public void setStatus(VacationStatus status) {
        this.status = status;
    }

    public static VacationRequestResponseRestDto from(VacationRequestResponseDto dto) {
        VacationRequestResponseRestDto response = new VacationRequestResponseRestDto();
        response.setId(dto.getId());
        response.setStartDate(dto.getStartDate());
        response.setEndDate(dto.getEndDate());
        response.setStatus(dto.getStatus());
        response.setEmployeeEmail(dto.getEmployeeEmail());
        return response;
    }

    @Override
    public String toString() {
        return "VacationRequestResponseRestDto{" +
            "id=" + id +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", status=" + status +
            ", employeeEmail='" + employeeEmail + '\'' +
            '}';
    }
}

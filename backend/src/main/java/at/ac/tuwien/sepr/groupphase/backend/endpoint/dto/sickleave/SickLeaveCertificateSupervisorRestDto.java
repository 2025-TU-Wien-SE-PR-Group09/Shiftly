package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateSupervisorDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SickLeaveCertificateSupervisorRestDto {

    private Long id;
    private LocalDateTime uploadedAt;
    private String employeeEmail;
    private LocalDate startDate;
    private LocalDate endDate;

    public SickLeaveCertificateSupervisorRestDto(Long id, LocalDateTime uploadedAt, String employeeEmail,
                                                 LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.uploadedAt = uploadedAt;
        this.employeeEmail = employeeEmail;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
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

    public static SickLeaveCertificateSupervisorRestDto from(
        SickLeaveCertificateSupervisorDto dto) {
        return new SickLeaveCertificateSupervisorRestDto(
            dto.getId(),
            dto.getUploadedAt(),
            dto.getEmployeeEmail(),
            dto.getStartDate(),
            dto.getEndDate()
        );
    }
}

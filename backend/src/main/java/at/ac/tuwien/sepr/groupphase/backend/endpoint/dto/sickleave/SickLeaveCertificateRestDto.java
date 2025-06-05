package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.validator.annotation.ValidDateRange;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning sick leave certificate metadata via REST API.
 */
@ValidDateRange
public class SickLeaveCertificateRestDto {

    private Long id;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadedAt;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private String employeeEmail;

    @NotNull(message = "Start date must not be null")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private LocalDate endDate;

    public SickLeaveCertificateRestDto(Long id, String fileName, String fileType, LocalDateTime uploadedAt, String employeeEmail, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadedAt = uploadedAt;
        this.employeeEmail = employeeEmail;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
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
}

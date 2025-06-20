package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Entity representing a sick leave certificate uploaded by an employee.
 * Stores file metadata, binary data, and upload timestamp.
 */
@Entity
public class SickLeaveCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the uploaded file.
     */
    private String fileName;

    /**
     * MIME type of the uploaded file.
     */
    private String fileType;

    /**
     * The binary content of the file.
     */
    @Lob
    private byte[] data;

    /**
     * Start Date of the uploaded file.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * End Date of the uploaded file.
     */
    @Column(nullable = false)
    private LocalDate endDate;


    /**
     * Reference to the employee (ApplicationUser) who uploaded the certificate.
     */
    @ManyToOne(optional = false)
    private ApplicationUser employee;

    /**
     * The timestamp when the file was uploaded.
     */
    private LocalDateTime uploadedAt;

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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ApplicationUser getEmployee() {
        return employee;
    }

    public void setEmployee(ApplicationUser employee) {
        this.employee = employee;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}

package at.ac.tuwien.sepr.groupphase.backend.service.dto.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SickLeaveEmailDto {

    @NotNull(message = "Email must not be null")
    @Size(max = 200, min = 5, message = "Email must be between 5 and 50 characters long")
    @Email(message = "Email must be a valid email address")
    private final String supervisorEmail;

    @NotNull(message = "First name must not be null")
    @Size(max = 50, min = 1, message = "First name must be between 1 and 50 characters long")
    private final String employeeFirstName;

    @NotNull(message = "Last name must not be null")
    @Size(max = 50, min = 1, message = "Last name must be between 1 and 50 characters long")
    private final String employeeLastName;

    @NotNull(message = "Department name must not be null")
    @Size(max = 50, min = 1, message = "Department name must be between 1 and 50 characters long")
    private final String departmentName;

    @NotNull(message = "Start date must not be null")
    private final LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private final LocalDate endDate;

    public SickLeaveEmailDto(String supervisorEmail, String employeeFirstName, String employeeLastName,
                             String departmentName, LocalDate startDate, LocalDate endDate) {
        this.supervisorEmail = supervisorEmail;
        this.employeeFirstName = employeeFirstName;
        this.employeeLastName = employeeLastName;
        this.departmentName = departmentName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getSupervisorEmail() {
        return supervisorEmail;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}

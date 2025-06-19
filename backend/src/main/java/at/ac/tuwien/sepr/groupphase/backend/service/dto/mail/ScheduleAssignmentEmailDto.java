package at.ac.tuwien.sepr.groupphase.backend.service.dto.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


/**
 * DTO used to notify an employee that they have been assigned to a shift.
 * Contains all relevant data to construct an informative email.
 */
public class ScheduleAssignmentEmailDto {

    @NotNull(message = "Email must not be null")
    @Size(max = 200, min = 5, message = "Email must be between 5 and 200 characters long")
    @Email(message = "Email must be a valid email address")
    private final String recipientMail;

    @NotNull(message = "First name must not be null")
    @Size(max = 50, min = 1, message = "First name must be between 1 and 50 characters long")
    private final String employeeFirstName;

    @NotNull(message = "Start date must not be null")
    private final LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private final LocalDate endDate;

    @NotNull(message = "Department name must not be null")
    @Size(max = 50, min = 1, message = "Department name must be between 1 and 50 characters long")
    private final String departmentName;


    public ScheduleAssignmentEmailDto(String recipientMail, String employeeFirstName, LocalDate startDate, LocalDate endDate, String departmentName) {
        this.recipientMail = recipientMail;
        this.employeeFirstName = employeeFirstName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.departmentName = departmentName;
    }

    public String getRecipientMail() {
        return recipientMail;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getDepartmentName() {
        return departmentName;
    }

}

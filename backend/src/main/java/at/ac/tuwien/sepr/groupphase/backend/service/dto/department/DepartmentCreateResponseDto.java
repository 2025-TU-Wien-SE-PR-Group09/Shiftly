package at.ac.tuwien.sepr.groupphase.backend.service.dto.department;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import java.util.Objects;

public class DepartmentCreateResponseDto {

    private Long id;

    @NotBlank(message = "Department name must not be blank")
    @Size(min = 1, max = 100, message = "Department name must be between 1 and 100 characters long")
    private String name;

    @NotBlank(message = "Supervisor email must not be blank")
    @Size(min = 6, max = 100, message = "Supervisor email must be between 6 and 100 characters long")
    @Email(message = "Supervisor email must be a valid email address")
    private String supervisorEmail;

    public DepartmentCreateResponseDto() {
    }

    public DepartmentCreateResponseDto(String name, String supervisorEmail) {
        this.name = name;
        this.supervisorEmail = supervisorEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupervisorEmail() {
        return supervisorEmail;
    }

    public void setSupervisorEmail(String supervisorEmail) {
        this.supervisorEmail = supervisorEmail;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DepartmentCreateResponseDto that)) {
            return false;
        }
        return Objects.equals(name, that.name)
            && Objects.equals(supervisorEmail, that.supervisorEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, supervisorEmail);
    }

    @Override
    public String toString() {
        return "DepartmentCreateResponseDto{"
            + "name='" + name + '\''
            + ", supervisorEmail='" + supervisorEmail + '\'' + '}';
    }
}
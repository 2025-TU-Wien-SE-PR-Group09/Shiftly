package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class DepartmentCreateRestDto {

    @NotBlank(message = "Department name must not be blank")
    private String name;

    @NotNull(message = "Supervisor email must not be null")
    @Email
    private String supervisorEmail;

    public DepartmentCreateRestDto() {
    }

    public DepartmentCreateRestDto(String name, String supervisorEmail) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DepartmentCreateRestDto that)) {
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
        return "DepartmentCreateRestDto{"
            + "name='"
            + name
            + '\''
            + ", supervisorEmail='"
            + supervisorEmail
            + '\'' + '}';
    }
}

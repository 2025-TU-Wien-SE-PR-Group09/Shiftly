package at.ac.tuwien.sepr.groupphase.backend.service.dto.department;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.department.DepartmentDetailRestResponseDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class DepartmentDetailResponseDto {

    private Long id;

    @NotBlank(message = "Department name must not be blank")
    @Size(min = 1, max = 100, message = "Department name must be between 1 and 100 characters long")
    private String name;

    @NotBlank(message = "Supervisor email must not be blank")
    @NotNull(message = "Supervisor email must not be null")
    @Size(min = 6, max = 100, message = "Supervisor email must be between 6 and 100 characters long")
    @Email(message = "Supervisor email must be a valid email address")
    private String supervisorEmail;

    public DepartmentDetailResponseDto() {
    }

    public DepartmentDetailResponseDto(Long id, String name, String supervisorEmail) {
        this.id = id;
        this.name = name;
        this.supervisorEmail = supervisorEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(o instanceof DepartmentDetailRestResponseDto that)) {
            return false;
        }
        return Objects.equals(name, that.getName())
            && Objects.equals(supervisorEmail, that.getSupervisorEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, supervisorEmail);
    }

    @Override
    public String toString() {
        return "DepartmentDetailResponseDto{"
            + "name='" + name + '\''
            + ", supervisorEmail='" + supervisorEmail + '\'' + '}';
    }
}

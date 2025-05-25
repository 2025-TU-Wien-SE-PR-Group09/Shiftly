package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentEditDto;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import java.util.Objects;

public class DepartmentEditRestDto {

    @NotBlank(message = "Old department name must not be blank")
    @Size(min = 1, max = 100, message = "Old department name must be between 1 and 100 characters long")
    private String oldName;

    @NotBlank(message = "New department name must not be blank")
    @Size(min = 1, max = 100, message = "New department name must be between 1 and 100 characters long")
    private String newName;

    @NotBlank(message = "Supervisor email must not be blank")
    @Size(min = 6, max = 100, message = "Supervisor email must be between 6 and 100 characters long")
    @Email(message = "Supervisor email must be a valid email address")
    private String supervisorEmail;

    public DepartmentEditRestDto() {
    }

    public DepartmentEditRestDto(String oldName, String newName, String supervisorEmail) {
        this.oldName = oldName;
        this.newName = newName;
        this.supervisorEmail = supervisorEmail;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
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
        if (!(o instanceof DepartmentEditRestDto that)) {
            return false;
        }
        return Objects.equals(oldName, that.oldName)
            && Objects.equals(newName, that.newName)
            && Objects.equals(supervisorEmail, that.getSupervisorEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldName, newName, supervisorEmail);
    }

    @Override
    public String toString() {
        return "DepartmentEditRestDto{"
            + "oldName='" + oldName + '\''
            + ", newName='" + newName + '\''
            + ", supervisorEmail='" + supervisorEmail + '\'' + '}';
    }
}


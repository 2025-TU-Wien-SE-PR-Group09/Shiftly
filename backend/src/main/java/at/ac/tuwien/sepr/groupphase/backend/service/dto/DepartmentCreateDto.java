package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import java.util.Objects;

public class DepartmentCreateDto {

    private String name;
    private String supervisorEmail;

    public DepartmentCreateDto() {
    }

    public DepartmentCreateDto(String name, String supervisorEmail) {
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
        if (!(o instanceof DepartmentCreateDto that)) {
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
        return "DepartmentCreateServiceDto{"
            + "name='" + name + '\''
            + ", supervisorEmail='" + supervisorEmail + '\'' + '}';
    }
}

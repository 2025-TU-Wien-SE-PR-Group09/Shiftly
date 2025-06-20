package at.ac.tuwien.sepr.groupphase.backend.service.dto.user;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UserRoleDto {
    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private String userEmail;

    @NotNull(message = "Role must not be null")
    private Role role;

    @NotNull
    private String departmentName;

    public UserRoleDto(String email, Role role, String departmentName) {
        this.userEmail = email;
        this.role = role;
        this.departmentName = departmentName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Role getRole() {
        return role;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
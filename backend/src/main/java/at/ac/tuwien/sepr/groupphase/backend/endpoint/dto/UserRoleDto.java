package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class UserRoleDto {
    @NotNull(message = "Email must not be null")
    @Email
    private String userEmail;

    @NotNull(message = "Role must not be null")
    private Role role;

    public UserRoleDto(String email, Role role) {
        this.userEmail = email;
        this.role = role;
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

    public void setRole(Role role) {
        this.role = role;
    }
}

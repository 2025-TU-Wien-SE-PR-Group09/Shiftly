package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for returning user profile data.
 * Contains name, email, role, and department information.
 */
public class UserProfileRestDto {

    @Size(min = 4, max = 100)
    private final String name;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private final String email;


    private final String role;
    private final String department;


    public UserProfileRestDto(String name, String email, String role, String department) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }


    @Override
    public String toString() {
        return "UserProfileRestDto{" +
            "name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", role='" + role + '\'' +
            ", department='" + department + '\'' +
            '}';
    }
}

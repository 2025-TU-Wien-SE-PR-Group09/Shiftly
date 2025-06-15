package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST DTO for returning user profile data.
 * Contains name, email, role, and department information.
 */
public class UserProfileRestDto {

    @Column(nullable = false, length = 50)
    @Size(max = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    @Size(max = 50)
    private String lastName;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private final String email;


    private final String role;
    private final String department;


    public UserProfileRestDto(String firstName, String lastName, String email, String role, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.department = department;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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
        return "UserProfileRestDto{"
            + "firstName='" + firstName + '\''
            + "lastName='" + lastName + '\''
            + ", email='" + email + '\''
            + ", role='" + role + '\''
            + ", department='" + department + '\''
            + '}';
    }
}

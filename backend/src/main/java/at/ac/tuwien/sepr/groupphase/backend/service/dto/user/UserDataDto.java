package at.ac.tuwien.sepr.groupphase.backend.service.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class UserDataDto {

    @NotNull(message = "Email must not be null")
    @Size(max = 200, min = 5, message = "Email must be between 5 and 50 characters long")
    @Email(message = "Email must be a valid email address")
    private String email;

    @Size(max = 50, min = 8, message = "Password must be between 8 and 50 characters long")
    @NotNull(message = "Password must not be null")
    private String password;

    @NotNull(message = "First name must not be null")
    @Size(max = 50, min = 1, message = "First name must be between 1 and 50 characters long")
    @Email(message = "First name must be a valid email address")
    private final String firstName;

    @NotNull(message = "Last name must not be null")
    @Size(max = 50, min = 1, message = "Last name must be between 1 and 50 characters long")
    @Email(message = "Last name must be a valid email address")
    private final String lastName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserDataDto(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDataDto userLoginDto)) {
            return false;
        }
        return Objects.equals(email, userLoginDto.email)
            && Objects.equals(password, userLoginDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "UserLoginDto{"
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + '}';
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}

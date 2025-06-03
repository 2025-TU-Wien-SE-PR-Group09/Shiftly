package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRestDto {
    @NotNull(message = "Email must not be null")
    @Size(max = 50, min = 1, message = "Email must be between 1 and 50 characters long")
    @Email
    private String email;

    @Size(max = 50, min = 8, message = "Password must be between 8 and 50 characters long")
    @NotNull(message = "Password must not be null")
    @Pattern(
        regexp = "^(?=.[a-z])(?=.[A-Z])(?=.*\\d).{8,}$",
        message = "New password must be at least 8 characters long and include uppercase, lowercase and a digit"
    )
    private String password;

    @NotNull(message = "Code must not be null")
    private String code;


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

    public String getCode() {
        return code;
    }

    public RegisterRestDto() {
    }

    public RegisterRestDto(String email, String password, String code) {
        this.email = email;
        this.password = password;
        this.code = code;
    }

    @Override
    public int hashCode() {
        return email.hashCode() + password.hashCode();
    }

    @Override
    public String toString() {
        return "RegisterDto{"
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + '}';
    }

    public static UserDataDto from(RegisterRestDto registerRestDto) {
        return new UserDataDto(
            registerRestDto.getEmail(),
            registerRestDto.getPassword()
        );
    }
}

package at.ac.tuwien.sepr.groupphase.backend.service.dto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataRestDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class UserDataDto {

    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotNull(message = "Password must not be null")
    private String password;

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

    public UserDataDto(String email, String password) {
        this.email = email;
        this.password = password;
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


    /**
     * Converts a {@link LoginResponseDto} to a {@link LoginResponseRestDto}.
     *
     * @param restDto the {@link LoginResponseDto} to convert
     * @return the converted {@link LoginResponseRestDto}
     */
    public static UserDataDto from(UserDataRestDto restDto) {
        return new UserDataDto(
            restDto.getEmail(),
            restDto.getPassword()
        );
    }
}

package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterTokenRequestDto(
    @NotNull(message = "Email must not be null")
    @Size(max = 200, min = 5, message = "Email must be between 5 and 50 characters long")
    @Email(message = "Email must be a valid email address")
    String email,

    @Size(max = 50, min = 8, message = "Password must be between 8 and 50 characters long")
    @NotNull(message = "Password must not be null")
    String password,

    @NotNull(message = "First name must not be null")
    @Size(max = 50, min = 1, message = "First name must be between 1 and 50 characters long")
    String firstName,

    @NotNull(message = "Last name must not be null")
    @Size(max = 50, min = 1, message = "Last name must be between 1 and 50 characters long")
    String lastName
) {

}

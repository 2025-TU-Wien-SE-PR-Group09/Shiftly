package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserInviteRequestDto(
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email
) {
}

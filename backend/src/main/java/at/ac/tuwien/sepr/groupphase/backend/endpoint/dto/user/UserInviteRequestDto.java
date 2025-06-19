package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserInviteRequestDto(
    @NotBlank(message = "Email darf nicht leer sein")
    @Email(message = "Ungültiges Email-Format")
    String email
) {
}

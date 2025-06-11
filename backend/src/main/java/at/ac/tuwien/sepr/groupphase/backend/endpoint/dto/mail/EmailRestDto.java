package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRestDto(
    @NotBlank(message = "Recipient email must not be blank")
    @Email(message = "Invalid email format")
    String to,

    @NotBlank(message = "Subject must not be blank")
    String subject,

    @NotBlank(message = "Email body must not be blank")
    String text
) {
}
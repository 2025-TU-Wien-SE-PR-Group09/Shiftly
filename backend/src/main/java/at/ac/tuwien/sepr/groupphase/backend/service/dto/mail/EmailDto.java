package at.ac.tuwien.sepr.groupphase.backend.service.dto.mail;

public record EmailDto(
    String to,
    String subject,
    String text
) {
}
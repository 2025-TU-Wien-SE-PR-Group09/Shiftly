package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.mail.EmailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EmailMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for sending emails manually.
 *
 * <p>This is primarily intended for testing and development purposes,
 * allowing developers or administrators to trigger email notifications
 * (e.g., using Postman or Swagger UI). In production, email sending
 * should typically be triggered automatically by business logic.
 */
@RestController
@RequestMapping("/api/v1/mail")
public class MailEndpoint {

    private final MailService mailService;
    private final EmailMapper emailMapper;

    public MailEndpoint(MailService mailService, EmailMapper emailMapper) {
        this.mailService = mailService;
        this.emailMapper = emailMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendMail(@RequestBody @Valid EmailRestDto emailRestDto) {
        var dto = emailMapper.toService(emailRestDto);
        mailService.sendSimpleEmail(dto.to(), dto.subject(), dto.text());
    }
}

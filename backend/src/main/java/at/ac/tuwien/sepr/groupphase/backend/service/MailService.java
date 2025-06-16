package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.SickLeaveEmailDto;
import org.springframework.scheduling.annotation.Async;

/**
 * Service interface for sending simple email messages.
 *
 * <p>This service provides functionality to send plain text emails.
 * Implementations of this interface may use different mail providers
 * or libraries (e.g. JavaMailSender).</p>
 *
 * <p>Typical use cases include sending notifications, alerts, or confirmations
 * to users such as sick leave notifications to supervisors.</p>
 */
public interface MailService {

    /**
     * Sends a simple plain text email to a specified recipient.
     *
     * @param to      the recipient's email address
     * @param subject the subject line of the email
     * @param text    the plain text content of the email
     */
    @Async
    void sendSimpleEmail(String to, String subject, String text);

    /**
     * Sends a formatted sick leave notification email to a supervisor.
     *
     * <p>The message includes the employee's name, department, and the sick leave period.
     * </p>
     *
     * @param dto the data transfer object containing all required email details
     */
    @Async
    void sendSickLeaveNotification(SickLeaveEmailDto dto);

}

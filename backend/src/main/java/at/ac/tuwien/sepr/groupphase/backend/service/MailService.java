package at.ac.tuwien.sepr.groupphase.backend.service;

public interface MailService {
    void sendSimpleEmail(String to, String subject, String text);
}

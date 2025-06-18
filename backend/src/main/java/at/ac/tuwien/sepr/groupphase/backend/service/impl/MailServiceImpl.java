package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.ScheduleAssignmentEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.SickLeaveEmailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import static at.ac.tuwien.sepr.groupphase.backend.util.DateFormatUtil.format;


@Service
public class MailServiceImpl implements MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JavaMailSender mailSender;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        LOGGER.trace("sendSimpleEmail({}, {}, {})", to, subject, text);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Override
    public void sendSickLeaveNotification(SickLeaveEmailDto dto) {
        LOGGER.trace("sendSickLeaveNotification({})", dto);

        String subject = "New Sick Leave Certificate";
        String text = String.format(
            "Employee %s %s reported a sick leave from %s to %s.",
            dto.getEmployeeFirstName(),
            dto.getEmployeeLastName(),
            format(dto.getStartDate()),
            format(dto.getEndDate())
        );

        sendSimpleEmail(dto.getSupervisorEmail(), subject, text);
    }

    @Override
    public void sendScheduleAssignmentNotification(ScheduleAssignmentEmailDto dto) {
        LOGGER.trace("sendScheduleAssignmentNotification({})", dto);
        String subject = "New Shift Assignments – " + dto.getDepartmentName();
        String text = String.format(
            """
                You have been assigned new shifts in the %s department.

                Please check your calendar in the system for details about your schedule.""",
            dto.getDepartmentName()
        );
        sendSimpleEmail(dto.getRecipientMail(), subject, text);
    }


}
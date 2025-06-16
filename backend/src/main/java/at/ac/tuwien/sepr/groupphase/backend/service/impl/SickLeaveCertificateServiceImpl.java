package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.SickLeaveCertificateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.SickLeaveCertificateService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.SickLeaveEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateUploadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Implementation of the SickLeaveCertificateService.
 * Handles the storage and retrieval of sick leave certificates,
 * including access restrictions for users and admins.
 */
@Service
public class SickLeaveCertificateServiceImpl implements SickLeaveCertificateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SickLeaveCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final MailService mailService;


    public SickLeaveCertificateServiceImpl(SickLeaveCertificateRepository certificateRepository,
                                           UserRepository userRepository,
                                           AuthService authService,
                                           MailService mailService) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.mailService = mailService;
    }

    @Override
    public SickLeaveCertificateDto upload(SickLeaveCertificateUploadDto uploadDto) throws NotFoundException, ConflictException {
        LOGGER.trace("upload({})", uploadDto);

        ApplicationUser user = userRepository.findByEmail(uploadDto.email())
            .orElseThrow(() -> new NotFoundException("User not found"));

        String generatedFileName = String.format("SickNote_%s_%s%s",
            uploadDto.startDate(),
            uploadDto.endDate(),
            getFileExtension(uploadDto.file().getOriginalFilename())
        );


        SickLeaveCertificate cert = new SickLeaveCertificate();
        cert.setFileName(generatedFileName);
        cert.setFileType(uploadDto.file().getContentType());
        cert.setUploadedAt(LocalDateTime.now());
        cert.setEmployee(user);
        cert.setStartDate(uploadDto.startDate());
        cert.setEndDate(uploadDto.endDate());

        try {
            cert.setData(uploadDto.file().getBytes());
        } catch (IOException e) {
            throw new ConflictException("Error reading file");
        }

        certificateRepository.save(cert);


        List<ApplicationUser> supervisors = userRepository.findSupervisorsByDepartment(user.getDepartment());
        for (ApplicationUser supervisor : supervisors) {
            try {
                SickLeaveEmailDto mailDto = new SickLeaveEmailDto(
                    supervisor.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getDepartment().getName(),
                    uploadDto.startDate(),
                    uploadDto.endDate()
                );

                mailService.sendSickLeaveNotification(mailDto);
                LOGGER.info("Successfully sent sick leave notification email to supervisor {}", supervisor.getEmail());
            } catch (Exception e) {
                LOGGER.warn("Failed to send sick leave email to supervisor {}: {}", supervisor.getEmail(), e.getMessage(), e);
            }
        }


        return new SickLeaveCertificateDto(
            cert.getId(),
            cert.getFileName(),
            cert.getFileType(),
            cert.getUploadedAt(),
            user.getEmail(),
            cert.getStartDate(),
            cert.getEndDate()
        );
    }

    @Override
    public SickLeaveCertificate findById(Long id) throws NotFoundException {
        LOGGER.trace("findById({})", id);

        return certificateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Certificate not found"));
    }

    @Override
    public List<SickLeaveCertificateDto> getAllForCurrentUser() {
        LOGGER.trace("getAllForCurrentUser()");

        String email = authService.getCurrentUser().getEmail();
        return certificateRepository.findByEmployee_Email(email).stream()
            .map(cert -> new SickLeaveCertificateDto(
                cert.getId(),
                cert.getFileName(),
                cert.getFileType(),
                cert.getUploadedAt(),
                cert.getEmployee().getEmail(),
                cert.getStartDate(),
                cert.getEndDate()
            ))
            .toList();
    }

    @Override
    public List<SickLeaveCertificateDto> getAllForAdmin() {
        LOGGER.trace("getAllForAdmin()");

        return certificateRepository.findAll().stream()
            .map(cert -> new SickLeaveCertificateDto(
                cert.getId(),
                cert.getFileName(),
                cert.getFileType(),
                cert.getUploadedAt(),
                cert.getEmployee().getEmail(),
                cert.getStartDate(),
                cert.getEndDate()
            ))
            .toList();
    }

    @Override
    public void deleteSickLeaveCertificate(Long id) throws NotFoundException, SecurityException {
        LOGGER.trace("deleteSickLeaveCertificate({})", id);

        ApplicationUser currentUser = authService.getCurrentUser();
        SickLeaveCertificate cert = certificateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Certificate not found"));

        boolean isOwner = currentUser.getEmail().equals(cert.getEmployee().getEmail());
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("You are not authorized to delete this certificate");
        }

        certificateRepository.delete(cert);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }


}

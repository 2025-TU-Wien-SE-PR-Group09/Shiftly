package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.SickLeaveCertificateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.SickLeaveCertificateService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.SickLeaveCertificateDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the SickLeaveCertificateService.
 * Handles the storage and retrieval of sick leave certificates,
 * including access restrictions for users and admins.
 */
@Service
public class SickLeaveCertificateServiceImpl implements SickLeaveCertificateService {
    private final SickLeaveCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public SickLeaveCertificateServiceImpl(SickLeaveCertificateRepository certificateRepository,
                                           UserRepository userRepository,
                                           AuthService authService) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Uploads a new sick leave certificate for the given user.
     *
     * @param file  the uploaded file
     * @param email the email of the uploader
     * @return metadata of the uploaded certificate
     */
    @Override
    public SickLeaveCertificateDto upload(MultipartFile file, String email) {
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        SickLeaveCertificate cert = new SickLeaveCertificate();
        cert.setFileName(file.getOriginalFilename());
        cert.setFileType(file.getContentType());
        cert.setUploadedAt(LocalDateTime.now());
        cert.setEmployee(user);

        try {
            cert.setData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }

        certificateRepository.save(cert);

        return new SickLeaveCertificateDto(
            cert.getId(),
            cert.getFileName(),
            cert.getFileType(),
            cert.getUploadedAt(),
            user.getEmail()
        );
    }

    /**
     * Finds a sick leave certificate by ID.
     *
     * @param id the certificate ID
     * @return the entity
     * @throws NotFoundException if not found
     */
    @Override
    public SickLeaveCertificate findById(Long id) {
        return certificateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Certificate not found"));
    }

    @Override
    public List<SickLeaveCertificate> findByEmployeeEmail(String email) {
        return certificateRepository.findByEmployee_Email(email);
    }

    @Override
    public List<SickLeaveCertificate> findAll() {
        return certificateRepository.findAll();
    }

    /**
     * Returns all sick leave certificates for the currently authenticated user.
     *
     * @return list of certificate DTOs
     */
    @Override
    public List<SickLeaveCertificateDto> getAllForCurrentUser() {
        String email = authService.getCurrentUser().getEmail();
        return certificateRepository.findByEmployee_Email(email).stream()
            .map(cert -> new SickLeaveCertificateDto(
                cert.getId(),
                cert.getFileName(),
                cert.getFileType(),
                cert.getUploadedAt(),
                cert.getEmployee().getEmail()
            ))
            .toList();
    }

    /**
     * Returns all sick leave certificates in the system (admin only).
     *
     * @return list of certificate DTOs
     */
    @Override
    public List<SickLeaveCertificateDto> getAllForAdmin() {
        return certificateRepository.findAll().stream()
            .map(cert -> new SickLeaveCertificateDto(
                cert.getId(),
                cert.getFileName(),
                cert.getFileType(),
                cert.getUploadedAt(),
                cert.getEmployee().getEmail()
            ))
            .toList();
    }
}

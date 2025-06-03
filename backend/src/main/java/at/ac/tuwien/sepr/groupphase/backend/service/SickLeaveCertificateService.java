package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SickLeaveCertificateService {
    SickLeaveCertificateDto upload(MultipartFile file, String email);

    SickLeaveCertificate findById(Long id);

    List<SickLeaveCertificate> findByEmployeeEmail(String email);

    List<SickLeaveCertificate> findAll();

    List<SickLeaveCertificateDto> getAllForCurrentUser();

    List<SickLeaveCertificateDto> getAllForAdmin();
}


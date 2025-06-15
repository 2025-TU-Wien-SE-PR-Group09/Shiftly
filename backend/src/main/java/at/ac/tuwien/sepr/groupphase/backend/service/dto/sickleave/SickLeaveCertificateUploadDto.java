package at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record SickLeaveCertificateUploadDto(MultipartFile file, String email, LocalDate startDate, LocalDate endDate) {
}

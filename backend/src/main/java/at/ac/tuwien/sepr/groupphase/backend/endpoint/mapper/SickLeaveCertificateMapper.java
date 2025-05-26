package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SickLeaveCertificateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.SickLeaveCertificateDto;
import org.springframework.stereotype.Component;

/**
 * Converts between SickLeaveCertificateDto and SickLeaveCertificateRestDto.
 */
@Component
public class SickLeaveCertificateMapper {
    public SickLeaveCertificateRestDto toRest(SickLeaveCertificateDto dto) {
        return new SickLeaveCertificateRestDto(
            dto.getId(),
            dto.getFileName(),
            dto.getFileType(),
            dto.getUploadedAt(),
            dto.getEmployeeEmail()
        );
    }
}

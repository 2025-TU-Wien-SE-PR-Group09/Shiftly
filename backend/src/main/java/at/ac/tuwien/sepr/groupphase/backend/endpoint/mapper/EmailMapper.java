package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.mail.EmailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.EmailDto;
import org.springframework.stereotype.Component;

@Component
public class EmailMapper {
    public EmailDto toService(EmailRestDto restDto) {
        return new EmailDto(
            restDto.to(),
            restDto.subject(),
            restDto.text()
        );
    }

    public EmailRestDto toRest(EmailDto serviceDto) {
        return new EmailRestDto(
            serviceDto.to(),
            serviceDto.subject(),
            serviceDto.text()
        );
    }
}

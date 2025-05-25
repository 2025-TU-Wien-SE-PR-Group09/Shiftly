package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
import java.util.List;

public interface VacationRequestService {
    VacationRequestResponseDto createVacationRequest(VacationRequestDto dto);

    List<VacationRequestResponseDto> getVacationRequestsForUser(UserEmailDto email);

    void deletePendingRequest(Long requestId, String userEmail);
}



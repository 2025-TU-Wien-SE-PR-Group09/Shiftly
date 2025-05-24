package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;

public interface VacationRequestService {
    VacationRequestResponseDto createVacationRequest(VacationRequestDto dto);
}

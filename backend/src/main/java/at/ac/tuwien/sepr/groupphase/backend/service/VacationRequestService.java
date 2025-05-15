package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;

public interface VacationRequestService {
    VacationRequestDto createVacationRequest(VacationRequestDto vacationRequestRestDto);
}

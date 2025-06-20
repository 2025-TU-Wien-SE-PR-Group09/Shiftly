package at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation;

import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;

public record UpdateVacationRequestStatusDto(Long id, VacationStatus newStatus) {
}

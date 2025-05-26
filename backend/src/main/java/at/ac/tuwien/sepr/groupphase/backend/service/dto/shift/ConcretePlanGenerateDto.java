package at.ac.tuwien.sepr.groupphase.backend.service.dto.shift;

import java.time.LocalDate;
import java.util.Optional;

public record ConcretePlanGenerateDto(Long planBlueprintId, Optional<LocalDate> startDate) {
}

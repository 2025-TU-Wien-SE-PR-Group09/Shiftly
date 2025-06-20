package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateConcretePlanDto(@NotNull(message = "Start date cannot be null")
                                      @FutureOrPresent(message = "Start date must be in the future!") LocalDate startDate) {
}

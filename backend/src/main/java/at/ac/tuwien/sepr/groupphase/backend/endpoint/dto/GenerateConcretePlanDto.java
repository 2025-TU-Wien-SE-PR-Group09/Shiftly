package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GenerateConcretePlanDto(@NotNull(message = "Start date cannot be null")
                                      @Future(message = "Start date must be in the future!") LocalDate startDate) {
}

package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response for department shift plan calendar")
public record DepartmentShiftplanCalendarResponse(List<ScheduledShift> shifts) {

    @Schema(description = "Scheduled shift details")
    public record ScheduledShift(String shiftDescription,
                                 Day day,
                                 @Schema(description = "List of workers(email) assigned to the shift")
                                 List<String> workers) {
    }

    @Schema(description = "Details of a day in the shift plan calendar")
    public record Day(
        @Schema(description = "Start of the day", example = "2023-10-01T08:00:00")
        LocalDateTime start,
        @Schema(description = "End of the day", example = "2023-10-01T16:00:00")
        LocalDateTime end
    ) {
    }
}

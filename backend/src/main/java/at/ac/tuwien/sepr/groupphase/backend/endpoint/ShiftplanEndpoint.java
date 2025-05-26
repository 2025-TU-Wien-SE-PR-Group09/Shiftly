package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentShiftplanCalendarResponse;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GenerateConcretePlanDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ScheduledShiftResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShiftRestMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Shiftplan")
@RestController
@RequestMapping("/api/shiftplans")
public class ShiftplanEndpoint {

    private final ShiftPlanningService shiftPlanningService;

    public ShiftplanEndpoint(ShiftPlanningService shiftPlanningService) {
        this.shiftPlanningService = shiftPlanningService;
    }




}

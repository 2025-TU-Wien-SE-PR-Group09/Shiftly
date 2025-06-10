package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanningService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shiftplan")
@RestController
@RequestMapping("/api/shiftplans")
public class ShiftplanEndpoint {

    private final ShiftPlanningService shiftPlanningService;

    public ShiftplanEndpoint(ShiftPlanningService shiftPlanningService) {
        this.shiftPlanningService = shiftPlanningService;
    }


}

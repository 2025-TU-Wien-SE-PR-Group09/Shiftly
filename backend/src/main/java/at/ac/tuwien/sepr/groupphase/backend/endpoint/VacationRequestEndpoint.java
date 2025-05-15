package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/vacation-request")
public class VacationRequestEndpoint {

    private final VacationRequestService vacationRequestService;

    /**
     * Constructor for the VacationRequestEndpoint class.
     *
     * @param vacationRequestService the service to handle vacation requests
     */
    public VacationRequestEndpoint(VacationRequestService vacationRequestService) {
        this.vacationRequestService = vacationRequestService;
    }

    @PostMapping
    @RolesAllowed({"EMPLOYEE"})
    public VacationRequestRestDto createVacationRequest(@RequestBody VacationRequestRestDto vacationRequestRestDto) {

        VacationRequestDto vacationRequestDto = new VacationRequestDto(
            null,
             null,
            vacationRequestRestDto.getStartDate(),
            vacationRequestRestDto.getEndDate(),
            "PENDING"
        );


        VacationRequestDto created = vacationRequestService.createVacationRequest(vacationRequestDto);


        VacationRequestRestDto response = new VacationRequestRestDto();
        response.setStartDate(created.getStartDate());
        response.setEndDate(created.getEndDate());
        response.setStatus(created.getStatus());


        return response;
    }
}

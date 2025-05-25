package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;


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


    /**
     * Endpoint to create a new vacation request.
     *
     * @param restDto   the vacation request data transfer object
     * @param principal the authenticated user principal
     * @return a response entity containing the created vacation request
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @RolesAllowed({"EMPLOYEE"})
    public ResponseEntity<VacationRequestResponseRestDto> createVacationRequest(@Valid @RequestBody VacationRequestRestDto restDto, Principal principal) {
        VacationRequestDto dto = VacationRequestDto.from(restDto);
        dto.setEmployeeEmail(principal.getName());
        VacationRequestResponseDto created = vacationRequestService.createVacationRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(VacationRequestResponseRestDto.from(created));
    }



    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({"EMPLOYEE"})
    public ResponseEntity<List<VacationRequestResponseRestDto>> getOwnVacationRequests(Principal principal) {
        List<VacationRequestResponseDto> dtos = vacationRequestService.getVacationRequestsForUser(principal.getName());
        List<VacationRequestResponseRestDto> restDtos = dtos.stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
        return ResponseEntity.ok(restDtos);
    }

}

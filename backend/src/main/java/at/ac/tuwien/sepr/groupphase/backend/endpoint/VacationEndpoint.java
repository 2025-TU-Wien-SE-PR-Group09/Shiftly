package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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


@RestController
@RequestMapping(value = "/api/v1/vacation-request")
public class VacationEndpoint {

    private final VacationRequestService vacationRequestService;

    /**
     * Constructor for the VacationRequestEndpoint class.
     *
     * @param vacationRequestService the service to handle vacation requests
     */
    public VacationEndpoint(VacationRequestService vacationRequestService) {
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
    @ApiResponse(responseCode = "201", description = "Vacation request created successfully")
    @Operation(summary = "Create a new vacation request")
    @RolesAllowed({"EMPLOYEE"})
    public ResponseEntity<VacationRequestResponseRestDto> createVacationRequest(
        @Valid @RequestBody VacationRequestRestDto restDto, Principal principal) {
        VacationRequestDto dto = VacationRequestDto.from(restDto);
        dto.setEmployeeEmail(principal.getName());
        VacationRequestResponseDto created = vacationRequestService.createVacationRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(VacationRequestResponseRestDto.from(created));
    }



    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({"EMPLOYEE"})
    public List<VacationRequestResponseRestDto> getOwnVacationRequests(Principal principal) {

        return vacationRequestService
            .getVacationRequestsForUser(new UserEmailDto(principal.getName()))
            .stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
    }

}

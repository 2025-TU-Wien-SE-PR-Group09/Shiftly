package at.ac.tuwien.sepr.groupphase.backend.endpoint;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation.VacationRequestResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @ApiResponse(responseCode = "200", description = "List of vacation requests retrieved")
    @RolesAllowed({"EMPLOYEE"})
    public List<VacationRequestResponseRestDto> getOwnVacationRequests(Principal principal) {

        return vacationRequestService
            .getVacationRequestsForUser(new UserEmailDto(principal.getName()))
            .stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
    }

    @DeleteMapping(path = "/{id}")
    @ApiResponse(responseCode = "204", description = "Vacation request deleted")
    @RolesAllowed("EMPLOYEE")
    @Transactional
    public ResponseEntity<Void> deleteVacationRequest(@PathVariable(name = "id") Long id, Principal principal) {
        vacationRequestService.deletePendingRequest(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed("SUPERVISOR")
    public List<VacationRequestResponseRestDto> getAllPendingRequests(Principal principal) {
        System.out.println("Supervisor: " + principal.getName());
        return vacationRequestService
            .getVacationRequestsByStatusAndSupervisor(VacationStatus.PENDING, principal.getName())
            .stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
    }

    @PutMapping(path = "/{id}/status")
    @RolesAllowed("SUPERVISOR")
    @Transactional
    public ResponseEntity<Void> updateVacationRequestStatus(
        @PathVariable("id") Long id,
        @RequestParam("newStatus") VacationStatus newStatus) {
        vacationRequestService.updateVacationRequestStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/approved", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed("SUPERVISOR")
    public List<VacationRequestResponseRestDto> getApprovedRequests(Principal principal) {
        return vacationRequestService
            .getVacationRequestsByStatusAndSupervisor(VacationStatus.APPROVED, principal.getName())
            .stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
    }





}

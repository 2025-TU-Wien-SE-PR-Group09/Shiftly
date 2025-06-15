package at.ac.tuwien.sepr.groupphase.backend.endpoint;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation.VacationRequestResponseRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.vacation.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.DeletePendingVacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.RetrieveVacationByStatusAndSupervisorDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.UpdateVacationRequestStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class VacationEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final VacationRequestService vacationRequestService;

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
        LOGGER.trace("createVacationRequest({}, {})", restDto, principal);

        VacationRequestDto dto = VacationRequestDto.from(restDto);
        dto.setEmployeeEmail(principal.getName());
        VacationRequestResponseDto created = vacationRequestService.createVacationRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(VacationRequestResponseRestDto.from(created));
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "List of vacation requests retrieved")
    @RolesAllowed({"EMPLOYEE"})
    @Operation(
        summary = "Get all vacation requests for the authenticated user",
        description = "Retrieves all vacation requests submitted by the currently authenticated user."
    )
    public List<VacationRequestResponseRestDto> getOwnVacationRequests(Principal principal) {
        LOGGER.trace("getOwnVacationRequests({})", principal);

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
    @Operation(
        summary = "Delete a pending vacation request",
        description = "Deletes a vacation request that is in the pending state. "
            + "The user must be authenticated and the request must be in pending status."
    )
    public ResponseEntity<Void> deleteVacationRequest(@PathVariable(name = "id") Long id, Principal principal) {
        LOGGER.trace("deleteVacationRequest({}, {})", id, principal);

        vacationRequestService.deletePendingRequest(new DeletePendingVacationRequestDto(id, principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed("SUPERVISOR")
    @Operation(
        summary = "Get all pending vacation requests for the supervisor",
        description = "Retrieves all vacation requests that are pending approval and assigned to the currently authenticated supervisor."
    )
    @ApiResponse(responseCode = "200", description = "List of pending vacation requests retrieved")
    public List<VacationRequestResponseRestDto> getAllPendingRequests(Principal principal) {
        LOGGER.trace("getAllPendingRequests({})", principal);

        System.out.println("Supervisor: " + principal.getName());
        return vacationRequestService
            .getVacationRequestsByStatusAndSupervisor(new RetrieveVacationByStatusAndSupervisorDto(
                VacationStatus.PENDING,
                principal.getName()))
            .stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
    }

    @PutMapping(path = "/{id}/status")
    @RolesAllowed("SUPERVISOR")
    @Transactional
    @Operation(
        summary = "Update the status of a vacation request",
        description = "Updates the status of a vacation request to either APPROVED or REJECTED. "
            + "The user must be authenticated and have supervisor privileges."
    )
    @ApiResponse(responseCode = "204", description = "Vacation request status updated successfully")
    public ResponseEntity<Void> updateVacationRequestStatus(
        @PathVariable("id") Long id,
        @RequestParam("newStatus") VacationStatus newStatus) {
        LOGGER.trace("updateVacationRequestStatus({}, {})", id, newStatus);

        vacationRequestService.updateVacationRequestStatus(new UpdateVacationRequestStatusDto(id, newStatus));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/approved", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed("SUPERVISOR")
    @Operation(
        summary = "Get all approved vacation requests for the supervisor",
        description = "Retrieves all vacation requests that have been approved and assigned to the currently authenticated supervisor."
    )
    @ApiResponse(responseCode = "200", description = "List of approved vacation requests retrieved")
    public List<VacationRequestResponseRestDto> getApprovedRequests(Principal principal) {
        LOGGER.trace("getApprovedRequests({})", principal);

        return vacationRequestService
            .getVacationRequestsByStatusAndSupervisor(
                new RetrieveVacationByStatusAndSupervisorDto(VacationStatus.APPROVED, principal.getName()))
            .stream()
            .map(VacationRequestResponseRestDto::from)
            .toList();
    }

}

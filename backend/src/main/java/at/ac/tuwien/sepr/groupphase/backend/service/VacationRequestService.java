package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.DeletePendingVacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.RetrieveVacationByStatusAndSupervisorDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.UpdateVacationRequestStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestResponseDto;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;


public interface VacationRequestService {
    /**
     * Creates a new vacation request.
     *
     * @param dto the vacation request data transfer object containing the details of the request
     * @return the created vacation request response data transfer object
     * @throws ConflictException if the request conflicts with existing vacation requests
     */
    VacationRequestResponseDto createVacationRequest(VacationRequestDto dto) throws ConflictException;

    /**
     * Retrieves all vacation requests for a specific user.
     *
     * @param email the email of the user whose vacation requests are to be retrieved
     * @return a list of vacation request response data transfer objects for the specified user
     * @throws NotFoundException if the user with the given email does not exist
     */
    List<VacationRequestResponseDto> getVacationRequestsForUser(UserEmailDto email) throws NotFoundException;

    /**
     * Deletes a pending vacation request by its ID and the user's email.
     *
     * @param requestDto the data transfer object containing the ID of the vacation request to be deleted
     * @throws AccessDeniedException if the user has no permissions to delete the vacation request
     * @throws NotFoundException if the vacation request with the given ID does not exist
     */
    void deletePendingRequest(DeletePendingVacationRequestDto requestDto)
        throws AccessDeniedException, NotFoundException;

    /**
     * Updates the status of a vacation request.
     *
     * @param updateDto the data transfer object containing the ID of the vacation request and the new status
     * @throws ConflictException if the status transition is not allowed
     * @throws NotFoundException if the vacation request with the given ID does not exist
     */
    void updateVacationRequestStatus(UpdateVacationRequestStatusDto updateDto)  throws ConflictException, NotFoundException;

    /**
     * Retrieves vacation requests by status and the supervisor's email.
     *
     * @param retrieveDto the data transfer object containing the status and supervisor's email
     * @return a list of vacation request response data transfer objects matching the criteria
     * @throws NotFoundException if the supervisor with the given email does not exist or does not have a valid department
     */
    List<VacationRequestResponseDto> getVacationRequestsByStatusAndSupervisor(RetrieveVacationByStatusAndSupervisorDto retrieveDto)
        throws NotFoundException;
}



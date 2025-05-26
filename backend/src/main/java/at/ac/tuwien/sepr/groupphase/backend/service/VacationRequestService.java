package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
import java.util.List;



public interface VacationRequestService {
    /**
     * Creates a new vacation request.
     *
     * @param dto the vacation request data transfer object containing the details of the request
     * @return the created vacation request response data transfer object
     */
    VacationRequestResponseDto createVacationRequest(VacationRequestDto dto);

    /**
     * Retrieves all vacation requests for a specific user.
     *
     * @param email the email of the user whose vacation requests are to be retrieved
     * @return a list of vacation request response data transfer objects for the specified user
     */
    List<VacationRequestResponseDto> getVacationRequestsForUser(UserEmailDto email);

    /**
     * Deletes a pending vacation request by its ID and the user's email.
     *
     * @param requestId the ID of the vacation request to be deleted
     * @param userEmail the email of the user who owns the vacation request
     */
    void deletePendingRequest(Long requestId, String userEmail);
}



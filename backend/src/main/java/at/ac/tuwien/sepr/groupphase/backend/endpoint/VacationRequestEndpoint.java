package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.VacationRequestRestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

   @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
       consumes = MediaType.APPLICATION_JSON_VALUE)
   @Transactional
   @RolesAllowed({"EMPLOYEE"})
   public ResponseEntity<VacationRequestRestDto> createVacationRequest(@RequestBody VacationRequestRestDto restDto) {
       VacationRequestDto dto = VacationRequestDto.from(restDto);
       VacationRequestDto created = vacationRequestService.createVacationRequest(dto);
       return ResponseEntity.status(HttpStatus.CREATED).body(VacationRequestRestDto.from(created));
   }
}

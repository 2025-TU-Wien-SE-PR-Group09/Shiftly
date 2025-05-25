package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VacationRequestServiceImpl implements VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final UserRepository userRepository;

    /**
     * Constructor for VacationRequestServiceImpl.
     *
     * @param vacationRequestRepository the repository for vacation requests
     * @param userRepository            the repository for users
     */
    @Autowired
    public VacationRequestServiceImpl(VacationRequestRepository vacationRequestRepository, UserRepository userRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new vacation request.
     *
     * @param vacationRequestDto the DTO containing the vacation request details
     * @return a DTO containing the created vacation request details
     */
    @Override
    public VacationRequestResponseDto createVacationRequest(VacationRequestDto vacationRequestDto) {
        LocalDate start = vacationRequestDto.getStartDate();
        LocalDate end = vacationRequestDto.getEndDate();

        ApplicationUser employee = userRepository.findByEmail(vacationRequestDto.getEmployeeEmail())
            .orElseThrow(() -> new NotFoundException("Logged in user not found"));

        VacationRequest vacationRequest = new VacationRequest();
        vacationRequest.setEmployee(employee);
        vacationRequest.setStartDate(vacationRequestDto.getStartDate());
        vacationRequest.setEndDate(vacationRequestDto.getEndDate());
        vacationRequest.setStatus(VacationStatus.PENDING);

        VacationRequest saved = vacationRequestRepository.save(vacationRequest);

        return new VacationRequestResponseDto(
            saved.getId(),
            employee.getEmail(),
            saved.getStartDate(),
            saved.getEndDate(),
            saved.getStatus()
        );
    }

    @Override
    public List<VacationRequestResponseDto> getVacationRequestsForUser(String email) {
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        List<VacationRequest> requests = vacationRequestRepository.findByEmployee(user);
        return requests.stream()
            .map(r -> new VacationRequestResponseDto(
                r.getId(),
                email,
                r.getStartDate(),
                r.getEndDate(),
                r.getStatus()
            ))
            .toList();
    }


}

package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VacationRequestServiceImpl implements VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public VacationRequestServiceImpl(VacationRequestRepository vacationRequestRepository, UserRepository userRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public VacationRequestDto createVacationRequest(VacationRequestDto vacationRequestDto) {
        LocalDate start = vacationRequestDto.getStartDate();
        LocalDate end = vacationRequestDto.getEndDate();

        //TODO: Validate the dates(Validator)
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Start date must be before end date and both must be provided.");
        }

        String userEmail = getCurrentUserEmail();
        ApplicationUser employee = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        VacationRequest vacationRequest = new VacationRequest();
        vacationRequest.setEmployee(employee);
        vacationRequest.setStartDate(start);
        vacationRequest.setEndDate(end);
        vacationRequest.setStatus("PENDING");

        VacationRequest saved = vacationRequestRepository.save(vacationRequest);

        return new VacationRequestDto(
            saved.getId(),
            userEmail,
            saved.getStartDate(),
            saved.getEndDate(),
            saved.getStatus()
        );
    }

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

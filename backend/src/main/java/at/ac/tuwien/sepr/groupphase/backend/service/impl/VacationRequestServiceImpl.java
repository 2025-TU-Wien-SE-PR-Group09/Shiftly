package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
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
    public VacationRequestResponseDto createVacationRequest(VacationRequestDto vacationRequestDto) {
        LocalDate start = vacationRequestDto.getStartDate();
        LocalDate end = vacationRequestDto.getEndDate();

        String userEmail = getCurrentUserEmail();
        ApplicationUser employee = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        VacationRequest vacationRequest = new VacationRequest();
        vacationRequest.setEmployee(employee);
        vacationRequest.setStartDate(vacationRequestDto.getStartDate());
        vacationRequest.setEndDate(vacationRequestDto.getEndDate());
        vacationRequest.setStatus(vacationRequestDto.getStatus());

        VacationRequest saved = vacationRequestRepository.save(vacationRequest);

        return new VacationRequestResponseDto(
            saved.getId(),
            employee.getEmail(),
            saved.getStartDate(),
            saved.getEndDate(),
            saved.getStatus()
        );
    }

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

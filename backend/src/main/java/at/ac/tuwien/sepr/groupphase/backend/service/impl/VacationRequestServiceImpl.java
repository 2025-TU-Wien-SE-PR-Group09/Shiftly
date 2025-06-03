package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.VacationRequestService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VacationRequestServiceImpl implements VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final UserRepository userRepository;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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


    @Override
    public VacationRequestResponseDto createVacationRequest(VacationRequestDto vacationRequestDto) {
        LocalDate start = vacationRequestDto.getStartDate();
        LocalDate end = vacationRequestDto.getEndDate();


        ApplicationUser employee = userRepository.findByEmail(vacationRequestDto.getEmployeeEmail())
            .orElseThrow(() -> new NotFoundException("Logged in user not found"));

        List<VacationRequest> existingRequests = vacationRequestRepository.findByEmployee(employee);

        LocalDate newStart = vacationRequestDto.getStartDate();
        LocalDate newEnd = vacationRequestDto.getEndDate();

        boolean overlaps = existingRequests.stream()
            .filter(req -> req.getStatus() != VacationStatus.REJECTED)
            .anyMatch(req ->
                !(newEnd.isBefore(req.getStartDate()) || newStart.isAfter(req.getEndDate()))
            );

        if (overlaps) {
            throw new ConflictException("Vacation request overlaps with existing approved or pending request");
        }


        Optional<ConcreteShiftPlan> shiftStartDate = employee.getDepartment().getShiftPlans().stream()
            .min(Comparator.comparing(ConcreteShiftPlan::getStartDate));
        if (shiftStartDate.isPresent() && start.isBefore(shiftStartDate.get().getStartDate()
            .plusWeeks(12).minusDays(1))) {
            throw new ConflictException("Vacation request are only allowed starting from the next shift cycle on"
                + shiftStartDate.get().getStartDate().plusWeeks(12)
                .minusDays(1).format(formatter));
        }


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
    public List<VacationRequestResponseDto> getVacationRequestsForUser(UserEmailDto email) {
        ApplicationUser user = userRepository.findByEmail(email.email())
            .orElseThrow(() -> new NotFoundException("User not found"));

        List<VacationRequest> requests = vacationRequestRepository.findByEmployee(user);
        return requests.stream()
            .map(r -> new VacationRequestResponseDto(
                r.getId(),
                email.email(),
                r.getStartDate(),
                r.getEndDate(),
                r.getStatus()
            ))
            .toList();
    }

    @Override
    public void deletePendingRequest(Long requestId, String userEmail) {
        VacationRequest request = vacationRequestRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Vacation request not found"));

        if (!request.getEmployee().getEmail().equals(userEmail)) {
            throw new ConflictException("User is not the owner of this request");
        }

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be deleted");
        }

        vacationRequestRepository.delete(request);
    }

    @Override
    public List<VacationRequestResponseDto> getAllPendingRequests() {
        return vacationRequestRepository.findAll().stream()
            .filter(r -> r.getStatus() == VacationStatus.PENDING)
            .map(r -> new VacationRequestResponseDto(
                r.getId(),
                r.getEmployee().getEmail(),
                r.getStartDate(),
                r.getEndDate(),
                r.getStatus()))
            .toList();
    }

    @Override
    public void updateVacationRequestStatus(Long id, VacationStatus newStatus) {
        VacationRequest request = vacationRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Vacation request not found"));

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new ConflictException("Only pending requests can be updated");
        }

        if (newStatus != VacationStatus.APPROVED && newStatus != VacationStatus.REJECTED) {
            throw new ConflictException("Invalid status transition");
        }

        request.setStatus(newStatus);
        vacationRequestRepository.save(request);
    }

    @Override
    public List<VacationRequestResponseDto> getVacationRequestsByStatus(VacationStatus status) {
        return vacationRequestRepository.findAll().stream()
            .filter(r -> r.getStatus() == status)
            .map(r -> new VacationRequestResponseDto(
                r.getId(),
                r.getEmployee().getEmail(),
                r.getStartDate(),
                r.getEndDate(),
                r.getStatus()
            ))
            .toList();
    }





}

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
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.DeletePendingVacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.RetrieveVacationByStatusAndSupervisorDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.UpdateVacationRequestStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;

import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class VacationRequestServiceImpl implements VacationRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final VacationRequestRepository vacationRequestRepository;
    private final UserRepository userRepository;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    public VacationRequestServiceImpl(VacationRequestRepository vacationRequestRepository, UserRepository userRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.userRepository = userRepository;
    }


    @Override
    public VacationRequestResponseDto createVacationRequest(VacationRequestDto vacationRequestDto) throws ConflictException {
        LOGGER.trace("createVacationRequest({})", vacationRequestDto);

        LocalDate start = vacationRequestDto.getStartDate();

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
    public List<VacationRequestResponseDto> getVacationRequestsForUser(UserEmailDto email) throws NotFoundException {
        LOGGER.trace("getVacationRequestsForUser({})", email);

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
    public void deletePendingRequest(DeletePendingVacationRequestDto requestDto)
        throws ConflictException, NotFoundException, IllegalStateException {
        LOGGER.trace("deletePendingRequest({})", requestDto);

        VacationRequest request = vacationRequestRepository.findById(requestDto.requestId())
            .orElseThrow(() -> new NotFoundException("Vacation request not found"));

        if (!request.getEmployee().getEmail().equals(requestDto.userEmail())) {
            throw new ConflictException("User is not the owner of this request");
        }

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be deleted");
        }

        vacationRequestRepository.delete(request);
    }

    @Override
    public void updateVacationRequestStatus(UpdateVacationRequestStatusDto updateDto) throws ConflictException, NotFoundException {
        LOGGER.trace("updateVacationRequestStatus({})", updateDto);

        VacationRequest request = vacationRequestRepository.findById(updateDto.id())
            .orElseThrow(() -> new NotFoundException("Vacation request not found"));

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new ConflictException("Only pending requests can be updated");
        }

        if (updateDto.newStatus() != VacationStatus.APPROVED && updateDto.newStatus() != VacationStatus.REJECTED) {
            throw new ConflictException("Invalid status transition");
        }

        request.setStatus(updateDto.newStatus());
        vacationRequestRepository.save(request);
    }

    @Override
    public List<VacationRequestResponseDto> getVacationRequestsByStatusAndSupervisor(RetrieveVacationByStatusAndSupervisorDto retrieveDto)
        throws NotFoundException, ConflictException {
        LOGGER.trace("getVacationRequestsByStatusAndSupervisor({})", retrieveDto);

        ApplicationUser supervisor = userRepository.findByEmail(retrieveDto.supervisorEmail())
            .orElseThrow(() -> new NotFoundException("Supervisor not found"));

        if (supervisor.getDepartment() == null) {
            throw new ConflictException("Supervisor is not assigned to any department");
        }

        String departmentName = supervisor.getDepartment().getName();

        List<VacationRequest> requests = vacationRequestRepository.findByStatusAndEmployeeDepartmentName(retrieveDto.status(), departmentName);

        return requests.stream()
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

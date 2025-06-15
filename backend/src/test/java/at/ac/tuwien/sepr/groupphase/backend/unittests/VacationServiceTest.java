package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VacationRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.DeletePendingVacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.vacation.VacationRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.VacationRequestServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.EMPLOYEE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.END_DATE;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.OVERLAPPING_END;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.OVERLAPPING_ID;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.OVERLAPPING_START;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.START_DATE;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.VACATION_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VacationServiceTest {

    private VacationRequestServiceImpl service;
    private VacationRequestRepository vacationRequestRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        vacationRequestRepository = mock(VacationRequestRepository.class);
        userRepository = mock(UserRepository.class);
        service = new VacationRequestServiceImpl(vacationRequestRepository, userRepository);
    }

    @Test
    void createVacationRequest_withOverlapping_shouldThrowValidationException() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(EMPLOYEE_EMAIL);

        VacationRequest overlapping = new VacationRequest();
        overlapping.setId(OVERLAPPING_ID);
        overlapping.setEmployee(user);
        overlapping.setStartDate(OVERLAPPING_START);
        overlapping.setEndDate(OVERLAPPING_END);
        overlapping.setStatus(VacationStatus.PENDING);

        VacationRequestDto dto = new VacationRequestDto(EMPLOYEE_EMAIL, START_DATE, END_DATE);

        when(userRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(user));
        when(vacationRequestRepository.findByEmployee(user)).thenReturn(List.of(overlapping));

        assertThrows(ConflictException.class, () -> service.createVacationRequest(dto));
    }

    @Test
    void createVacationRequest_withUnknownUser_shouldThrowNotFoundException() {
        VacationRequestDto dto = new VacationRequestDto("unknown@example.com", START_DATE, END_DATE);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createVacationRequest(dto));
    }

    @Test
    void deletePendingRequest_byNonOwner_shouldThrowConflictException() {
        ApplicationUser otherUser = new ApplicationUser();
        otherUser.setEmail("someoneelse@example.com");

        VacationRequest request = new VacationRequest();
        request.setId(VACATION_ID);
        request.setEmployee(otherUser);
        request.setStatus(VacationStatus.PENDING);

        when(vacationRequestRepository.findById(VACATION_ID)).thenReturn(Optional.of(request));

        assertThrows(ConflictException.class, () -> service.deletePendingRequest(new DeletePendingVacationRequestDto(VACATION_ID, EMPLOYEE_EMAIL)));
    }

    @Test
    void deletePendingRequest_validRequest_shouldDeleteSuccessfully() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(EMPLOYEE_EMAIL);

        VacationRequest request = new VacationRequest();
        request.setId(VACATION_ID);
        request.setEmployee(user);
        request.setStatus(VacationStatus.PENDING);

        when(vacationRequestRepository.findById(VACATION_ID)).thenReturn(Optional.of(request));


        assertDoesNotThrow(() -> service.deletePendingRequest(new DeletePendingVacationRequestDto(VACATION_ID, EMPLOYEE_EMAIL)));
        verify(vacationRequestRepository, times(1)).delete(request);
    }

    @Test
    void getVacationRequestsForUser_existingUser_shouldReturnList() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(EMPLOYEE_EMAIL);

        VacationRequest request1 = new VacationRequest();
        request1.setId(1L);
        request1.setEmployee(user);
        request1.setStartDate(START_DATE);
        request1.setEndDate(END_DATE);
        request1.setStatus(VacationStatus.PENDING);

        VacationRequest request2 = new VacationRequest();
        request2.setId(2L);
        request2.setEmployee(user);
        request2.setStartDate(START_DATE.plusDays(15));
        request2.setEndDate(END_DATE.plusDays(15));
        request2.setStatus(VacationStatus.APPROVED);

        when(userRepository.findByEmail(EMPLOYEE_EMAIL)).thenReturn(Optional.of(user));
        when(vacationRequestRepository.findByEmployee(user)).thenReturn(List.of(request1, request2));

        var result = service.getVacationRequestsForUser(new UserEmailDto(EMPLOYEE_EMAIL));

        assertEquals(2, result.size());
        assertEquals(VacationStatus.PENDING, result.get(0).getStatus());
        assertEquals(VacationStatus.APPROVED, result.get(1).getStatus());
    }

    @Test
    void deletePendingRequest_withApprovedStatus_shouldThrowIllegalStateException() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(EMPLOYEE_EMAIL);

        VacationRequest request = new VacationRequest();
        request.setId(VACATION_ID);
        request.setEmployee(user);
        request.setStatus(VacationStatus.APPROVED);

        when(vacationRequestRepository.findById(VACATION_ID)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () ->
            service.deletePendingRequest(new DeletePendingVacationRequestDto(VACATION_ID, EMPLOYEE_EMAIL)));
    }

    @Test
    void deletePendingRequest_notExisting_shouldThrowNotFoundException() {
        when(vacationRequestRepository.findById(VACATION_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            service.deletePendingRequest(new DeletePendingVacationRequestDto(VACATION_ID, EMPLOYEE_EMAIL)));
    }


}

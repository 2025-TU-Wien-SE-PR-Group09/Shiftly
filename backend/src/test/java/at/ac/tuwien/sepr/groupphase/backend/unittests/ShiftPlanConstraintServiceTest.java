package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.shift.ShiftPlanConstraintServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ShiftPlanConstraintServiceTest {

    private ShiftPlanConstraintService serviceUnderTest;
    private UserRepository userRepository;
    private VacationRequestRepository vacationRequestRepository;
    private ScheduledShiftRepository scheduledShiftRepository;
    private TimeService timeService;

    @BeforeEach
    void setUp() {
        timeService = mock(TimeService.class);
        when(timeService.now()).thenReturn(LocalDateTime.now());

        userRepository = mock(UserRepository.class);
        vacationRequestRepository = mock(VacationRequestRepository.class);
        scheduledShiftRepository = mock(ScheduledShiftRepository.class);

        serviceUnderTest = new ShiftPlanConstraintServiceImpl(
            userRepository,
            vacationRequestRepository,
            scheduledShiftRepository
        );
    }

    @Test
    void testJumperAssignedForAllVacationDaysOfEmployee() {
        // Arrange
        ApplicationUser jumper = createJumper("jumper@shyft.local");
        ApplicationUser max = createUser("max@shyft.local");

        when(userRepository.findAllByRoleName("JUMPER"))
            .thenReturn(List.of(jumper));

        // Vacation for max from Monday to Tuesday
        LocalDate weekStart = LocalDate.of(2025, 6, 2);
        VacationRequest vacationRequest = new VacationRequest();
        vacationRequest.setEmployee(max);
        vacationRequest.setStartDate(weekStart);
        vacationRequest.setEndDate(weekStart.plusDays(1));
        vacationRequest.setStatus(VacationStatus.APPROVED);
        when(vacationRequestRepository.findByEmployeeAndStatus(max, VacationStatus.APPROVED))
            .thenReturn(List.of(vacationRequest));

        // No other jumper shifts scheduled
        when(scheduledShiftRepository.findByStartBetween(any(), any()))
            .thenReturn(List.of());

        // Plan with shifts for Max
        ScheduledShift shiftMonday = createShift(weekStart.atTime(8, 0), max);
        ScheduledShift shiftTuesday = createShift(weekStart.plusDays(1).atTime(8, 0), max);
        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        plan.addScheduledShifts(List.of(shiftMonday, shiftTuesday));
        plan.setStartDate(weekStart);

        // Act
        ConcreteShiftPlan updatedPlan = serviceUnderTest.applyConstraints(plan);

        // Assert: Jumper should be assigned to both shifts
        for (ScheduledShift shift : updatedPlan.getScheduledShifts()) {
            List<String> assignedEmails = shift.getAssignments().stream()
                .map(a -> a.getUser().getEmail())
                .collect(Collectors.toList());

            assertTrue(assignedEmails.contains("jumper@shyft.local"));
            assertFalse(assignedEmails.contains("max@shyft.local"));
        }
    }

    @Test
    void testConflictWhenJumperAlreadyAssignedToOtherShiftSameDay() {
        // Arrange
        ApplicationUser jumper = createJumper("jumper@shyft.local");
        ApplicationUser max = createUser("max@shyft.local");
        ApplicationUser tina = createUser("tina@shyft.local");

        when(userRepository.findAllByRoleName("JUMPER"))
            .thenReturn(List.of(jumper));

        LocalDate weekStart = LocalDate.of(2025, 6, 2);

        // Max vacation on Monday and Tuesday
        VacationRequest vacationRequestMax = new VacationRequest();
        vacationRequestMax.setEmployee(max);
        vacationRequestMax.setStartDate(weekStart);
        vacationRequestMax.setEndDate(weekStart.plusDays(1));
        vacationRequestMax.setStatus(VacationStatus.APPROVED);
        when(vacationRequestRepository.findByEmployeeAndStatus(max, VacationStatus.APPROVED))
            .thenReturn(List.of(vacationRequestMax));

        // Tina vacation on Tuesday and Wednesday
        VacationRequest vacationRequestTina = new VacationRequest();
        vacationRequestTina.setEmployee(tina);
        vacationRequestTina.setStartDate(weekStart.plusDays(1));
        vacationRequestTina.setEndDate(weekStart.plusDays(2));
        vacationRequestTina.setStatus(VacationStatus.APPROVED);
        when(vacationRequestRepository.findByEmployeeAndStatus(tina, VacationStatus.APPROVED))
            .thenReturn(List.of(vacationRequestTina));

        // Jumper already assigned to a shift on Tuesday
        ScheduledShift existingShift = createShift(weekStart.plusDays(1).atTime(8, 0), jumper);
        when(scheduledShiftRepository.findByStartBetween(any(), any()))
            .thenReturn(List.of(existingShift));

        // Plan with shifts for Max and Tina
        ScheduledShift shiftMaxTuesday = createShift(weekStart.plusDays(1).atTime(8, 0), max);
        ScheduledShift shiftTinaTuesday = createShift(weekStart.plusDays(1).atTime(10, 0), tina);
        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        plan.addScheduledShifts(List.of(shiftMaxTuesday, shiftTinaTuesday));
        plan.setStartDate(weekStart);

        // Act & Assert
        assertThrows(ConflictException.class, () -> serviceUnderTest.applyConstraints(plan));
    }

    @Test
    void testJumperAssignedConsistentlyForAllVacationDays() {
        // Setup
        ApplicationUser jumper1 = createJumper("jumper1@shift.local");
        ApplicationUser max = createUser("max@shift.local");

        when(userRepository.findAllByRoleName("JUMPER")).thenReturn(List.of(jumper1));

        // Max: Vacation from Monday to Tuesday
        LocalDate weekStart = LocalDate.of(2025, 6, 9);
        VacationRequest vacationRequest = new VacationRequest();
        vacationRequest.setEmployee(max);
        vacationRequest.setStartDate(weekStart);
        vacationRequest.setEndDate(weekStart.plusDays(1));
        vacationRequest.setStatus(VacationStatus.APPROVED);
        when(vacationRequestRepository.findByEmployeeAndStatus(max, VacationStatus.APPROVED))
            .thenReturn(List.of(vacationRequest));

        // No other jumper shifts scheduled
        when(scheduledShiftRepository.findByStartBetween(any(), any()))
            .thenReturn(List.of());

        // Plan with shifts for Max on Monday and Tuesday
        ScheduledShift mondayShift = createShift(weekStart.atTime(8, 0), max);
        ScheduledShift tuesdayShift = createShift(weekStart.plusDays(1).atTime(8, 0), max);
        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        plan.setStartDate(weekStart);
        plan.addScheduledShifts(List.of(mondayShift, tuesdayShift));

        // Action
        ConcreteShiftPlan updatedPlan = serviceUnderTest.applyConstraints(plan);

        // Assertions
        assertShiftHasAssignedUser(updatedPlan, weekStart, jumper1);
        assertShiftHasAssignedUser(updatedPlan, weekStart.plusDays(1), jumper1);
    }


    // Helper
    private ApplicationUser createUser(String email) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        return user;
    }

    private ApplicationUser createJumper(String email) {
        ApplicationUser jumper = createUser(email);

        ApplicationRole jumperRole = new ApplicationRole("JUMPER");

        jumper.getRoles().add(jumperRole);
        jumperRole.getUsers().add(jumper);

        return jumper;
    }


    private ScheduledShift createShift(LocalDateTime start, ApplicationUser assignedUser) {
        ScheduledShift shift = new ScheduledShift.Builder()
            .withStart(start)
            .withEnd(start.plusHours(8))
            .withDescription("Test shift")
            .withPlan(new ConcreteShiftPlan())
            .build();

        ScheduledShiftAssignment assignment = new ScheduledShiftAssignment.Builder()
            .withShift(shift)
            .withUser(assignedUser)
            .build();

        shift.addAssignment(assignment);
        return shift;
    }

    private void assertShiftHasAssignedUser(ConcreteShiftPlan plan, LocalDate date, ApplicationUser expectedUser) {
        ScheduledShift shift = plan.getScheduledShifts().stream()
            .filter(s -> s.getStart().toLocalDate().equals(date))
            .findFirst()
            .orElseThrow();

        Set<ApplicationUser> assignedUsers = shift.getAssignments().stream()
            .map(ScheduledShiftAssignment::getUser)
            .collect(Collectors.toSet());

        assertThat(assignedUsers).containsExactly(expectedUser);
    }
}

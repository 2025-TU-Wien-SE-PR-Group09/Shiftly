package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShiftPlanConstraintServiceImpl implements ShiftPlanConstraintService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final VacationRequestRepository vacationRequestRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;

    public ShiftPlanConstraintServiceImpl(UserRepository userRepository,
                                          VacationRequestRepository vacationRequestRepository,
                                          ScheduledShiftRepository scheduledShiftRepository) {
        this.userRepository = userRepository;
        this.vacationRequestRepository = vacationRequestRepository;
        this.scheduledShiftRepository = scheduledShiftRepository;
    }

    @Override
    public ConcreteShiftPlan applyConstraints(ConcreteShiftPlan concreteShiftPlan) {
        LOGGER.trace("applyConstraints({})", concreteShiftPlan);

        Map<LocalDate, Set<ApplicationUser>> availableJumpersPerDay =
            getAvailableJumperEmployeesPerDay(concreteShiftPlan);


        for (ScheduledShift shift : concreteShiftPlan.getScheduledShifts()) {
            LocalDate shiftDate = shift.getStart().toLocalDate();
            LocalDate weekStart = shiftDate.with(DayOfWeek.MONDAY);
            Set<ApplicationUser> availableJumpersToday = availableJumpersPerDay.get(shiftDate);

            List<ScheduledShiftAssignment> assignmentsCopy = new ArrayList<>(shift.getAssignments());

            for (ScheduledShiftAssignment assignment : assignmentsCopy) {
                ApplicationUser assignedUser = assignment.getUser();
                List<LocalDate> overlap = getVacationOverlap(assignedUser, weekStart);

                if (overlap != null && overlap.contains(shiftDate)) {
                    if(!availableJumpersToday.isEmpty()) {
                        ApplicationUser jumper = availableJumpersToday.stream().findFirst().get();

                        availableJumpersPerDay.get(shiftDate).remove(jumper);

                        shift.getAssignments().remove(assignment);
                        shift.addAssignment(new ScheduledShiftAssignment.Builder()
                            .withShift(shift)
                            .withUser(jumper)
                            .build());
                    }
                    else {
                        shift.getAssignments().remove(assignment);
                    }


                    /*

                    // Remove the overlap for the assigned user after assigning a jumper
                    // This is in case there is funky behavior with multiple shifts on the same day
                    // Might not be necessary

                    overlap.remove(shiftDate);
                    if (overlap.isEmpty()) {
                        vacationReplacementMap.remove(assignedUser);

                        for (int i = 0; i < 7; i++) {
                            LocalDate day = weekStart.plusDays(i);
                            if (day.isAfter(shiftDate)) {
                                availableJumpersPerDay.get(day).add(jumper);
                            }
                        }
                    }
                     */
                }
            }
        }

        return concreteShiftPlan;
    }


    /**
     * Get all employees who are available to work as jumpers in the given week.
     *
     * @param weekStart The start date of the week to check.
     * @return A list of available jumper employees.
     */
    private List<ApplicationUser> getAvailableJumperEmployees(LocalDate weekStart) {
        LOGGER.trace("getAvailableJumperEmployees({})", weekStart);

        List<ApplicationUser> jumperUsers = userRepository.findAllByRoleName("JUMPER");

        LocalDate weekEnd = weekStart.plusDays(6);
        List<ScheduledShift> shiftsInWeek = scheduledShiftRepository.findByStartBetween(
            weekStart.atStartOfDay(),
            weekEnd.atTime(LocalTime.MAX)
        );

        Set<ApplicationUser> assignedJumpers = shiftsInWeek.stream()
            .flatMap(shift -> shift.getAssignments().stream())
            .map(ScheduledShiftAssignment::getUser)
            .filter(jumperUsers::contains)
            .collect(Collectors.toSet());

        return jumperUsers.stream()
            .filter(jumper -> !assignedJumpers.contains(jumper))
            .toList();
    }


    /**
     * Get the vacation overlap for a specific user in a given week.
     *
     * @param user      The user to check.
     * @param weekStart The start date of the week to check.
     * @return List of LocalDate representing the overlap period, or null if no overlap.
     */
    private List<LocalDate> getVacationOverlap(ApplicationUser user, LocalDate weekStart) {
        LOGGER.trace("getVacationOverlap({}, {})", user, weekStart);

        LocalDate weekEnd = weekStart.plusDays(6);

        return vacationRequestRepository.findByEmployeeAndStatus(user, VacationStatus.APPROVED).stream()
            .filter(request ->
                request.getStartDate().isBefore(weekEnd.plusDays(1))
                    && request.getEndDate().isAfter(weekStart.minusDays(1)))
            .map(request -> {
                LocalDate overlapStart = request.getStartDate().isBefore(weekStart) ? weekStart : request.getStartDate();
                LocalDate overlapEnd = request.getEndDate().isAfter(weekEnd) ? weekEnd : request.getEndDate();
                List<LocalDate> overlapDays = new ArrayList<>();
                for (LocalDate date = overlapStart; !date.isAfter(overlapEnd); date = date.plusDays(1)) {
                    overlapDays.add(date);
                }
                return overlapDays;
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * Get a map of available jumper employees for each day of the week starting from the given date.
     *
     * @param concreteShiftPlan The ConcreteShiftPlan to get the available jumpers for.
     * @return A map where keys are LocalDate and values are sets of available ApplicationUser.
     */
    private Map<LocalDate, Set<ApplicationUser>> getAvailableJumperEmployeesPerDay(ConcreteShiftPlan concreteShiftPlan) {
        LOGGER.trace("getAvailableJumperEmployeesPerDay({})", concreteShiftPlan);

        List<ApplicationUser> jumperUsers = userRepository.findAllByRoleName("JUMPER");

        var department = concreteShiftPlan.getDepartment();
        var weekStart = concreteShiftPlan.getStartDate();
        Set<ApplicationUser> jumperUsersInDepartment = jumperUsers.stream()
            .filter(user -> user.getDepartment().equals(department))
            .collect(Collectors.toSet());

        Map<LocalDate, Set<ApplicationUser>> result = new HashMap<>();
        for (int i = 0; i < ChronoUnit.DAYS.between(concreteShiftPlan.getStartDate(), concreteShiftPlan.getEndDate()); i++) {
            LocalDate day = weekStart.plusDays(i);
            result.put(day, new HashSet<>(jumperUsersInDepartment));
        }

        LocalDate weekEnd = weekStart.plusDays(6);
        List<ScheduledShift> shiftsInWeek = scheduledShiftRepository.findByStartBetween(
            weekStart.atStartOfDay(),
            weekEnd.atTime(LocalTime.MAX)
        );

        for (ScheduledShift shift : shiftsInWeek) {
            LocalDate day = shift.getStart().toLocalDate();
            for (ScheduledShiftAssignment assignment : shift.getAssignments()) {
                result.get(day).remove(assignment.getUser());
            }
        }

        return result;
    }

}

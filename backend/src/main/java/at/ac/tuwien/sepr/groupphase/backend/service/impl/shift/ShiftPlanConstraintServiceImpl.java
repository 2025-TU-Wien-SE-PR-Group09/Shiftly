package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VacationRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import at.ac.tuwien.sepr.groupphase.backend.type.VacationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Map<ApplicationUser, ApplicationUser> vacationReplacementMap = new HashMap<>();

        HashMap<ApplicationUser, List<LocalDate>> overlaps = new HashMap<>();
        concreteShiftPlan.getScheduledShifts()
            .stream()
            .map(ScheduledShift::getAssignments)
            .flatMap(Collection::stream)
            .map(ScheduledShiftAssignment::getUser).distinct()
            .forEach(u -> {
                List<VacationRequest> vacationRequests = vacationRequestRepository.findByEmployeeEmailAndStatus(u.getEmail(), VacationStatus.APPROVED);

                overlaps.put(u, getVacationOverlap(vacationRequests,
                    concreteShiftPlan.getStartDate(),
                    concreteShiftPlan.getEndDate()));
            });




        for (ScheduledShift shift : concreteShiftPlan.getScheduledShifts()) {
            LocalDate shiftDate = shift.getStart().toLocalDate();
            List<ScheduledShiftAssignment> assignmentsCopy = new ArrayList<>(shift.getAssignments());

            // Reduction from missing employee problem
            // to find jumper for worker in vacation problem
            if (shift.getManpower() > assignmentsCopy.size()) {
                // If the shift has more manpower than assignments, we need to add jumpers
                var availableJumpers =  availableJumpersPerDay.get(shiftDate);
                int requiredJumpers = Math.min(shift.getManpower() - assignmentsCopy.size(), availableJumpers.size());


                // Add jumpers to the shift until the required manpower is reached
                for (int i = 0; i < requiredJumpers; i++) {
                    var placeholderUser = new ApplicationUser();
                    placeholderUser.setEmail("placeholder-jumper-" + i + "@" + shift.getPlan().getDepartment().getName() + ".com");
                    placeholderUser.setFirstName("Placeholder");
                    placeholderUser.setLastName("Jumper " + i);

                    assignmentsCopy.add(new ScheduledShiftAssignment.Builder()
                        .withShift(shift)
                        .withUser(placeholderUser)
                        .build());

                    overlaps.put(placeholderUser, List.of(shiftDate)); // No vacation overlap for placeholder jumpers
                }
            }

            Set<ApplicationUser> availableJumpersToday = availableJumpersPerDay.get(shiftDate);

            // Sort assignments by the number of overlaps of the user who is assigned to the shift.
            // This way, we can prioritize users with more overlaps when assigning jumpers.
            assignmentsCopy.sort(Comparator.comparingInt(a -> overlaps.get(((ScheduledShiftAssignment) a).getUser()) == null ? 0 : overlaps.get(((ScheduledShiftAssignment) a).getUser()).size()).reversed());

            for (ScheduledShiftAssignment assignment : assignmentsCopy) {
                ApplicationUser assignedUser = assignment.getUser();
                List<LocalDate> overlap = overlaps.get(assignedUser);

                if (overlap != null && overlap.contains(shiftDate)) {
                    if (!availableJumpersToday.isEmpty()) {
                        ApplicationUser jumper = null;

                        // Check if the assigned user has a vacation replacement already
                        // todo: overlap is wrong. a shift is only a day and not a week
                        if (vacationReplacementMap.containsKey(assignedUser) && availableJumpersToday.contains(vacationReplacementMap.get(assignedUser))) {
                            jumper = vacationReplacementMap.get(assignedUser);
                        } else {
                            // Find a jumper who is available on all overlap days
                            for (ApplicationUser jumperCandidate : availableJumpersToday) {
                                boolean availableOnAllDays = true;

                                for (LocalDate date : overlap) {
                                    if (!availableJumpersPerDay.get(date).contains(jumperCandidate)) {
                                        availableOnAllDays = false;
                                    }
                                }

                                if (!availableOnAllDays) {
                                    jumper = jumperCandidate;
                                    break;
                                }
                            }

                            // If no jumper was found, use the first available jumper
                            // As an optimization, when no jumper is found to be free on all days, we can try to find
                            // two jumpers that are available on the overlap days.
                            if (jumper != null) {
                                vacationReplacementMap.put(assignedUser, jumper);
                            } else {
                                jumper = availableJumpersToday.stream().findFirst().get();
                            }
                        }

                        availableJumpersPerDay.get(shiftDate).remove(jumper);

                        shift.getAssignments().remove(assignment);

                        LOGGER.info(assignment.toString());
                        LOGGER.info(jumper.getEmail());
                        LOGGER.info(shiftDate.toString());

                        new ScheduledShiftAssignment.Builder()
                                .withId(new ScheduledShiftAssignmentId(
                                    assignment.getId().getScheduledShiftId(),
                                    jumper.getEmail()
                                ))
                            .withShift(shift)
                            .withUser(jumper)
                            .build();
                    } else {
                        shift.getAssignments().remove(assignment);
                    }
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
    private List<ApplicationUser> getAvailableJumperEmployees(LocalDate weekStart, Department department) {
        LOGGER.trace("getAvailableJumperEmployees({})", weekStart);

        List<ApplicationUser> jumperUsers = userRepository.findJumpersByDepartment(department);

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
     * Get the vacation overlap for a list of vacations in a given week.
     *
     * @param vacationRequests      The vacation requests to verify
     * @param weekStart The start date of the week to check.
     * @return List of LocalDate representing the overlap period, or null if no overlap.
     */
    private List<LocalDate> getVacationOverlap(List<VacationRequest> vacationRequests, LocalDate weekStart, LocalDate weekEnd) {
        LOGGER.trace("getVacationOverlap({}, {})", vacationRequests, weekStart);

        return vacationRequests.stream()
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

        List<ApplicationUser> jumperUsersInDepartment = userRepository.findJumpersByDepartment(concreteShiftPlan.getDepartment());

        var department = concreteShiftPlan.getDepartment();
        var weekStart = concreteShiftPlan.getStartDate();

        Map<LocalDate, Set<ApplicationUser>> result = new HashMap<>();
        for (int i = 0; i <= ChronoUnit.DAYS.between(concreteShiftPlan.getStartDate(), concreteShiftPlan.getEndDate()); i++) {
            LocalDate day = weekStart.plusDays(i);
            result.put(day, new HashSet<>(jumperUsersInDepartment));
        }

        LocalDate weekEnd = weekStart.plusDays(6);
        // TODO this is problematic if we ever allow to edit a concrete shift plan
        // TODO as it will take into account shifts that were in the old plan
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

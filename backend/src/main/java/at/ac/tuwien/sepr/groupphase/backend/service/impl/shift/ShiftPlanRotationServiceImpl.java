package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.logic.ShiftRotator;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftAssignmentAuditLogRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanRotationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShiftPlanRotationServiceImpl implements ShiftPlanRotationService {


    private final TimeService timeService;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private final ShiftAssignmentAuditLogRepository shiftAssignmentAuditLogRepository;

    public ShiftPlanRotationServiceImpl(TimeService timeService, ConcreteShiftPlanRepository concreteShiftPlanRepository, ShiftAssignmentAuditLogRepository shiftAssignmentAuditLogRepository) {
        this.timeService = timeService;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.shiftAssignmentAuditLogRepository = shiftAssignmentAuditLogRepository;
    }

    @Override
    public ConcreteShiftPlan generateRotatingPlan(ConcreteShiftPlan concreteShiftPlan, PlanBlueprint planBlueprint, List<ApplicationUser> employees) {
        ShiftRotator rotator = new ShiftRotator(planBlueprint);
        rotator.assignInitialUsers(employees);

        List<ScheduledShift> allShifts = new ArrayList<>();
        List<ShiftAssignmentAuditLog> logs = new ArrayList<>();

        for (int weekOffset = 0; weekOffset < 12; weekOffset++) {
            LocalDate weekStart = concreteShiftPlan.getStartDate().plusWeeks(weekOffset);

            var slots = rotator.getNextCycle();

            for (var slot : slots) {
                for (var day : slot.week().getDays()) {
                    LocalDateTime shiftStart = weekStart.with(day.getDay()).atTime(day.getStartTime());
                    LocalDateTime shiftEnd = shiftStart.plus(day.getDuration());

                    ScheduledShift shift = new ScheduledShift.Builder()
                        .withStart(shiftStart)
                        .withEnd(shiftEnd)
                        .withDescription(slot.blueprint().getDescription())
                        .withPlan(concreteShiftPlan)
                        .build();

                    for (ApplicationUser user : slot.assignedUsers()) {
                        shift.addAssignment(new ScheduledShiftAssignment(shift, user));

                        logs.add(new ShiftAssignmentAuditLog.Builder()
                            .withTimestamp(timeService.now())
                            .withWorker(user)
                            .withTrigger(ShiftAssignmentTrigger.INITIAL_ASSIGNMENT_ALGORITHM)
                            .withShift(shift)
                            .build());
                    }

                    allShifts.add(shift);
                }
            }
        }

        concreteShiftPlan.addScheduledShifts(allShifts);
        ConcreteShiftPlan result = concreteShiftPlanRepository.save(concreteShiftPlan);
        shiftAssignmentAuditLogRepository.saveAll(logs);

        return result;

    }
}

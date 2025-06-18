package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.logic.ShiftRotator;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftAssignmentAuditLogRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.ScheduleAssignmentEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanRotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShiftPlanRotationServiceImpl implements ShiftPlanRotationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TimeService timeService;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private final ShiftAssignmentAuditLogRepository shiftAssignmentAuditLogRepository;
    private final MailService mailService;

    public ShiftPlanRotationServiceImpl(TimeService timeService, ConcreteShiftPlanRepository concreteShiftPlanRepository, ShiftAssignmentAuditLogRepository shiftAssignmentAuditLogRepository, MailService mailService) {
        this.timeService = timeService;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.shiftAssignmentAuditLogRepository = shiftAssignmentAuditLogRepository;
        this.mailService = mailService;
    }

    @Override
    public ConcreteShiftPlan generateRotatingPlan(ConcreteShiftPlan concreteShiftPlan, PlanBlueprint planBlueprint, List<ApplicationUser> employees) {
        LOGGER.trace("generateRotatingPlan({}, {}, {})", concreteShiftPlan, planBlueprint, employees);

        ShiftRotator rotator = new ShiftRotator(planBlueprint);
        rotator.assignInitialUsers(employees);

        List<ScheduledShift> allShifts = new ArrayList<>();
        List<ShiftAssignmentAuditLog> logs = new ArrayList<>();
        Set<ApplicationUser> notifiedUsers = new HashSet<>();

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
                        .withManpower(slot.blueprint().getManPower())
                        .build();

                    for (ApplicationUser user : slot.assignedUsers()) {
                        shift.addAssignment(new ScheduledShiftAssignment(shift, user));

                        logs.add(new ShiftAssignmentAuditLog.Builder()
                            .withTimestamp(timeService.now())
                            .withWorker(user)
                            .withTrigger(ShiftAssignmentTrigger.INITIAL_ASSIGNMENT_ALGORITHM)
                            .withShift(shift)
                            .build());

                        notifiedUsers.add(user);
                    }

                    allShifts.add(shift);
                }
            }
        }

        concreteShiftPlan.addScheduledShifts(allShifts);
        ConcreteShiftPlan result = concreteShiftPlanRepository.save(concreteShiftPlan);
        shiftAssignmentAuditLogRepository.saveAll(logs);

        for (ApplicationUser user : notifiedUsers) {
            try {
                ScheduleAssignmentEmailDto dto = new ScheduleAssignmentEmailDto(
                    user.getEmail(),
                    user.getDepartment().getName()
                );
                mailService.sendScheduleAssignmentNotification(dto);
                LOGGER.info("Sent schedule notification to {}", user.getEmail());
            } catch (Exception e) {
                LOGGER.warn("Failed to send schedule email to {}: {}", user.getEmail(), e.getMessage(), e);
            }
        }

        return result;

    }
}

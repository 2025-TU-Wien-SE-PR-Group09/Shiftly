package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftId;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftAssignmentAuditLog;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftAssignmentTrigger;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftAssignmentAuditLogRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftPlanningValidator;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@Service
public class ShiftPlanningServiceImpl implements ShiftPlanningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PlanBlueprintRepository planBlueprintRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeService timeService;
    private final ShiftPlanningValidator shiftPlanningValidator;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final ShiftAssignmentAuditLogRepository shiftAssignmentAuditRepository;

    public ShiftPlanningServiceImpl(TimeService timeService,
                                    ShiftPlanningValidator shiftPlanningValidator,
                                    PlanBlueprintRepository planBlueprintRepository,
                                    DepartmentRepository departmentRepository,
                                    ConcreteShiftPlanRepository concreteShiftPlanRepository,
                                    ScheduledShiftRepository scheduledShiftRepository,
                                    ShiftAssignmentAuditLogRepository shiftAssignmentAuditRepository) {
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftPlanningValidator = shiftPlanningValidator;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.scheduledShiftRepository = scheduledShiftRepository;
        this.shiftAssignmentAuditRepository = shiftAssignmentAuditRepository;
    }

    @Override
    public PlanBlueprintDto createPlanBlueprint(PlanBlueprintCreationDto createPlanBlueprintDto) {
        var dept = departmentRepository.findById(createPlanBlueprintDto.departmentName())
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        var planBuilder = new PlanBlueprint.Builder()
            .withDepartment(dept)
            .withDescription(createPlanBlueprintDto.description())
            .intercept(p -> shiftPlanningValidator.validatePlan(p).ifPresent(errors -> {
                throw new ConflictException(errors);
            }));

        createPlanBlueprintDto.shifts().forEach(shiftDto -> {

            var shiftBuilder = new ShiftBlueprint.Builder()
                .withDescription(shiftDto.description())
                .withManPower(shiftDto.manpower())
                .intercept(s -> shiftPlanningValidator.validateShift(s).ifPresent(errors -> {
                    throw new ConflictException(errors);
                }));

            AtomicInteger weekIndex = new AtomicInteger();
            shiftDto.weeks().forEach(weekDto -> {

                var days = weekDto.days().stream()
                    .map(day -> new ShiftDayBlueprint.Builder()
                        .withDay(day.dayOfWeek().orElse(null))
                        .withStartTime(day.startTime().orElse(null))
                        .withDuration(day.duration().orElse(null))
                        .intercept(d -> shiftPlanningValidator.validateDay(d).ifPresent(errors -> {
                            throw new ConflictException(errors);
                        }))
                        .build()).toList();

                var week = new ShiftWeekBlueprint.Builder()
                    .withIndex(weekIndex.getAndIncrement())
                    .withDays(days)
                    .intercept(w -> shiftPlanningValidator.validateWeek(w).ifPresent(errors -> {
                        throw new ConflictException(errors);
                    }))
                    .build();

                shiftBuilder.addWeek(week);
            });

            var shift = shiftBuilder.build();

            planBuilder.addShift(shift);
        });
        PlanBlueprint plan = planBuilder.build();

        shiftPlanningValidator.validateDescriptions(plan.getShifts())
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });

        shiftPlanningValidator.validateManpowerMinimum(plan.getShifts())
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });


        shiftPlanningValidator.validateWeeklyDurationsPerPlan(plan.getShifts(), plan)
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });


        plan = this.planBlueprintRepository.save(plan);
        return ShiftPlanningMapper.Plans.fromEntity(plan);
    }

    @Override
    public PlanBlueprintDto addShiftToPlan(PlanBlueprintAddShiftDto addShiftDto) {
        var plan = this.planBlueprintRepository.findById(addShiftDto.planId())
            .orElseThrow(() -> new NotFoundException("Plan not found!"));

        PlanBlueprint finalPlan1 = plan;
        addShiftDto.shifts().forEach(shiftDto -> {
            var shiftBuilder = new ShiftBlueprint.Builder()
                .withDescription(shiftDto.description())
                .withManPower(shiftDto.manPower())
                .intercept(shift -> shiftPlanningValidator.validateShift(shift).ifPresent(errors -> {
                    throw new ConflictException(errors);
                }));

            AtomicInteger weekIndex = new AtomicInteger();

            shiftDto.weeks().forEach(weekDto -> {
                var days = weekDto.days().stream()
                    .map(day -> new ShiftDayBlueprint.Builder()
                        .withDay(day.dayOfWeek().orElse(null))
                        .withStartTime(day.startTime().orElse(null))
                        .withDuration(day.duration().orElse(null))
                        .intercept(d -> shiftPlanningValidator.validateDay(d).ifPresent(errors -> {
                            throw new ConflictException(errors);
                        }))
                        .build())
                    .toList();

                var week = new ShiftWeekBlueprint.Builder()
                    .withIndex(weekIndex.getAndIncrement())
                    .withDays(days)
                    .intercept(w -> shiftPlanningValidator.validateWeek(w).ifPresent(errors -> {
                        throw new ConflictException(errors);
                    }))
                    .build();

                shiftBuilder.addWeek(week);
            });

            var shift = shiftBuilder.build();
            finalPlan1.addShiftBlueprint(shift);
        });

        List<ShiftBlueprint> allShifts = new ArrayList<>(plan.getShifts());

        shiftPlanningValidator.validateDescriptions(allShifts)
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });

        shiftPlanningValidator.validateManpowerMinimum(allShifts)
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });


        shiftPlanningValidator.validateConsistentWeekCountPerPlan(allShifts, plan)

            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });

        shiftPlanningValidator.validateOverlappingShiftsPerPlan(allShifts, plan)
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });

        shiftPlanningValidator.validateWeeklyDurationsPerPlan(allShifts, plan)
            .ifPresent(errors -> {
                throw new ConflictException(errors);
            });


        plan = this.planBlueprintRepository.save(plan);
        // validate whole plan after changes
        shiftPlanningValidator.validatePlan(plan).ifPresent(errors -> {
            throw new ConflictException(errors);
        });

        var updated = planBlueprintRepository.save(plan);
        return ShiftPlanningMapper.Plans.fromEntity(updated);
    }


    @Override
    @Transactional
    public ConcreteShiftPlan generateConcreteQuarterlyPlan(ConcretePlanGenerateDto dto) {
        PlanBlueprint blueprint = planBlueprintRepository.findById(dto.planBlueprintId())
            .orElseThrow(() -> new NotFoundException("Plan blueprint not found"));

        Department department = blueprint.getDepartment();
        LocalDate month = dto.startDate().orElseThrow(() -> new ConflictException("Start date is required"));
        LocalDate startDate = timeService.nextMondayInMonth(month);

        concreteShiftPlanRepository.findByDepartmentName(department.getName()).stream()
            .min((a, b) -> b.getEndDate().compareTo(a.getEndDate())).ifPresent(concreteShiftPlan -> {
                if (concreteShiftPlan.getEndDate().isAfter(startDate)) {
                    throw new ConflictException("A concrete plan for this department already exists for the specified period.");
                }
            });

        LocalDate endDate = startDate.plusWeeks(11); // 12 Wochen

        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        plan.setDepartment(department);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);

        Predicate<ApplicationUser> isNotSupervisor =
            u -> u.getRoles().stream().noneMatch(r -> r.getName().equals(Role.SUPERVISOR.name()));

        List<ApplicationUser> allUsers = plan.getDepartment().getUsers().stream().toList()
            .stream()
            .filter(isNotSupervisor)
            .toList();

        var weeks = blueprint.getShifts().stream().flatMap(s -> s.getShiftWeeks().stream())
            .map(x -> new ShiftWeekRotationNode(x.getShiftBlueprint(), x))
            .toList();

        var c = weeks.stream()
            .sorted(Comparator.<ShiftWeekRotationNode, String>comparing(x -> x.blueprint.getDescription())
                .thenComparingInt(x -> x.week.getWeekIndex()))
            .toList();

        var a = weeks.stream()
            .sorted(Comparator.<ShiftWeekRotationNode>comparingInt(x -> x.week.getWeekIndex())
                .thenComparing(x -> x.week.getDays().stream()
                    .map(ShiftDayBlueprint::getStartTime)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalTime.MAX)
                ))
            .toList();

        Queue<ShiftWeekRotationNode> rotationQueue = new LinkedList<>();
        int size = weeks.size();
        for (int i = 0; i < size; i++) {
            rotationQueue.add(a.get(i));
            ShiftWeekRotationNode current = c.get(i);
            current.next = c.get((i + 1) % size);
            current.prev = c.get((i - 1 + size) % size);
        }

        var mutable = new ArrayList<>(allUsers);
        a.stream().limit(blueprint.getShifts().size()).forEach(
            x -> {
                int i = 0;
                while (i < x.blueprint.getManPower()) {
                    var user = mutable.removeFirst();
                    x.users.add(user);
                    i++;
                }
            });

        var logs = new ArrayList<ShiftAssignmentAuditLog>();
        var allShifts = new ArrayList<ScheduledShift>();
        for (int week = 0; week < 12; week++) {
            LocalDate weekStart = startDate.plusWeeks(week);
            int weekSize = 2;
            for (int y = 0; y < weekSize; y++) {
                var node = rotationQueue.poll();
                assert node != null;
                for (ShiftDayBlueprint day : node.week.getDays()) {
                    LocalDateTime shiftStart = weekStart.with(day.getDay()).atTime(day.getStartTime());
                    LocalDateTime shiftEnd = shiftStart.plus(day.getDuration());

                    ScheduledShift scheduledShift = new ScheduledShift.Builder()
                        .withDepartmentName(department.getName())
                            .withStart(shiftStart)
                            .withEnd(shiftEnd)
                            .withDescription(shiftBlueprint.getDescription())
                            .withPlan(plan)
                            .build();

                    node.users.forEach(u -> {
                        scheduledShift.addAssignment(new ScheduledShiftAssignment(scheduledShift, u));

                        logs.add(new ShiftAssignmentAuditLog.Builder()
                            .withTimestamp(timeService.now())
                            .withWorker(u)
                            .withTrigger(ShiftAssignmentTrigger.INITIAL_ASSIGNMENT_ALGORITHM)
                            .withShift(scheduledShift)
                            .build());
                    });

                    allShifts.add(scheduledShift);
                }
                node.rotate();
                rotationQueue.add(node);
            }
        }


        plan.addScheduledShifts(allShifts);
        ConcreteShiftPlan result = concreteShiftPlanRepository.save(plan);
        shiftAssignmentAuditRepository.saveAll(logs);
        return result;
    }


    @Override
    public ConcreteShiftPlan getCurrentConcretePlan(String departmentName) {
        return concreteShiftPlanRepository.findByDepartmentName(departmentName)
            .stream()
            .max(Comparator.comparing(ConcreteShiftPlan::getStartDate))
            .orElseThrow(() -> new NotFoundException(("No current concrete plan found for department with ID: " + departmentName)));
    }

    private static class ShiftWeekRotationNode {
        ShiftBlueprint blueprint;
        ShiftWeekBlueprint week;
        int manpower;
        Deque<ApplicationUser> users = new ArrayDeque<>();
        ShiftWeekRotationNode next;
        ShiftWeekRotationNode prev;

        ShiftWeekRotationNode(ShiftBlueprint blueprint, ShiftWeekBlueprint week) {
            this.blueprint = blueprint;
            this.week = week;
            this.manpower = blueprint.getManPower();
        }

        void rotate() {
            if (users.isEmpty()) return;

            // Übergabe an nächste Woche
            for (int i = 0; i < Math.min(manpower, next.manpower); i++) {
                if (users.isEmpty()) break;
                ApplicationUser user = users.removeLast();
                next.users.addFirst(user);
            }

            // Überschuss an vorige Woche
            for (int i = 0; i < manpower - Math.min(manpower, next.next.manpower); i++) {
                if (users.isEmpty()) break;
                ApplicationUser user = users.removeLast();
                fill(user);
            }
        }

        public void fill(ApplicationUser user) {
            while (prev != null) {
                if (prev.users.size() < prev.manpower) {
                    prev.users.addLast(user);
                    return;
                }
                prev = prev.prev;
            }
        }

        @Override
        public String toString() {
            String name = (blueprint != null ? blueprint.getDescription() : "unknown") + "-W" + week.getWeekIndex();
            String prevName = (prev != null && prev.blueprint != null)
                ? prev.blueprint.getDescription() + "-W" + prev.week.getWeekIndex()
                : "null";
            String nextName = (next != null && next.blueprint != null)
                ? next.blueprint.getDescription() + "-W" + next.week.getWeekIndex()
                : "null";

            return String.format(
                "[%s | manpower=%d | users=%s | prev=%s | next=%s]",
                name,
                manpower,
                users.stream().map(ApplicationUser::getEmail).toList(),
                prevName,
                nextName
            );
        }
    }


}
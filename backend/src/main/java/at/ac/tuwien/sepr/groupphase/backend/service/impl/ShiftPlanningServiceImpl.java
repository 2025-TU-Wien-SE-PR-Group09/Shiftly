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
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ScheduledShiftDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftPlanningValidator;
import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
            ShiftAssignmentAuditLogRepository shiftAssignmentAuditRepository,
            ) {
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
                .withDescription(createPlanBlueprintDto.description());

        createPlanBlueprintDto.shifts().forEach(shiftDto -> {
            final var shiftBuilder = planBuilder.addShift(shift -> {
                shift.withDescription(shiftDto.description())
                        .withManPower(shiftDto.manpower());

                AtomicInteger i = new AtomicInteger();
                shiftDto.weeks().forEach(weekDto -> {
                    shift.addWeek(i.getAndIncrement(), weekBuilder -> {
                        weekBuilder.withDays(weekDto.days().stream()
                                .map(day -> {
                                    var d = new ShiftDayBlueprint.Builder()
                                            .withDay(day.dayOfWeek().get())
                                            .withStartTime(day.startTime().get())
                                            .withDuration(day.duration().get())
                                            .build();

                                    shiftPlanningValidator.validateDay(d).ifPresent(errors -> {
                                        throw new ConflictException(errors);
                                    });

                                    return d;
                                })
                                .toList());
                    });
                });
            });
        });
        PlanBlueprint plan = planBuilder.build();

        plan = planBuilder.build();

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

        // TODO: validation
        var newShifts = addShiftDto.shifts().stream().map(shiftDto -> {
            var shift = new ShiftBlueprint.Builder()
                    .withDescription(shiftDto.description())
                    .withManPower(shiftDto.manPower())
                    .build();

            AtomicInteger i = new AtomicInteger();
            var weeks = shiftDto.weeks().stream().map(weekDto -> new ShiftWeekBlueprint.Builder()
                .withIndex(i.getAndIncrement())
                .withDays(weekDto.days().stream()
                    .map(day -> new ShiftDayBlueprint.Builder()
                        .withDay(day.dayOfWeek().get())
                        .withStartTime(day.startTime().get())
                        .withDuration(day.duration().get())
                        .build())
                    .toList())
                .build()).toList();

            shift.addWeeks(weeks);

            return shift;
        }).toList();

        List<ShiftBlueprint> allShifts = new ArrayList<>(plan.getShifts());
        allShifts.addAll(newShifts);

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




        final var finalPlan = plan;
        newShifts.forEach(shift -> {
            shift.setPlan(finalPlan);
            finalPlan.addShiftBlueprint(shift);
        });

        plan = this.planBlueprintRepository.save(plan);

        return ShiftPlanningMapper.Plans.fromEntity(plan);
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

        List<ScheduledShift> allShifts = new ArrayList<>();
        Map<ShiftBlueprint, List<ApplicationUser>> rotationMap = prepareRotation(blueprint);
        List<ShiftAssignmentAuditLog> logs = new LinkedList<>();

        for (int weekIndex = 0; weekIndex < 12; weekIndex++) {
            LocalDate weekStart = startDate.plusWeeks(weekIndex);

            for (ShiftBlueprint shiftBlueprint : blueprint.getShifts()) {
                int blueprintWeekIndex = weekIndex % shiftBlueprint.getShiftWeeks().size();
                ShiftWeekBlueprint weekTemplate = shiftBlueprint.getShiftWeeks().get(blueprintWeekIndex);

                for (ShiftDayBlueprint day : weekTemplate.getDays()) {
                    LocalDateTime shiftStart = weekStart.with(day.getDay()).atTime(day.getStartTime());
                    LocalDateTime shiftEnd = shiftStart.plus(day.getDuration());

                    ScheduledShift scheduledShift = new ScheduledShift.Builder()
                        .withDepartmentName(department.getName())
                            .withStart(shiftStart)
                            .withEnd(shiftEnd)
                            .withDescription(shiftBlueprint.getDescription())
                            .withPlan(plan)
                            .build();

                    // Rotierender Mitarbeiterkreis
                    List<ApplicationUser> candidates = rotationMap.get(shiftBlueprint);
                    int manpower = shiftBlueprint.getManPower();
                    int offset = (weekIndex * manpower) % candidates.size();

                    for (int i = 0; i < manpower; i++) {
                        ApplicationUser user = candidates.get((offset + i) % candidates.size());
                        ScheduledShiftAssignment assignment = new ScheduledShiftAssignment(scheduledShift, user);
                        scheduledShift.addAssignment(assignment);

                        var log = new ShiftAssignmentAuditLog.Builder()
                                .withTimestamp(this.timeService.now())
                                .withWorker(user)
                                .withTrigger(ShiftAssignmentTrigger.INITIAL_ASSIGNMENT_ALGORITHM)
                                .withShift(scheduledShift)
                                .build();

                        logs.add(log);
                    }

                    allShifts.add(scheduledShift);
                }
            }
        }

        plan.addScheduledShifts(allShifts);
        var result = concreteShiftPlanRepository.save(plan);
        this.shiftAssignmentAuditRepository.saveAll(logs);
        return result;
    }


    @Override
    public ConcreteShiftPlan getCurrentConcretePlan(String departmentName) {
        return concreteShiftPlanRepository.findByDepartmentName(departmentName)
            .stream()
            .max(Comparator.comparing(ConcreteShiftPlan::getStartDate))
            .orElseThrow(() -> new NotFoundException(("No current concrete plan found for department with ID: " + departmentName)));
    }

}
package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftAssignmentAuditLog;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftAssignmentTrigger;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.logic.RotatingShiftSlot;
import at.ac.tuwien.sepr.groupphase.backend.logic.ShiftRotator;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftAssignmentAuditLogRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanRotationService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanRotationService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftPlanningValidator;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final ShiftPlanRotationService shiftPlanRotationService;
    private final ShiftPlanConstraintService shiftPlanConstraintService;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;

    public ShiftPlanningServiceImpl(TimeService timeService,
                                    ShiftPlanningValidator shiftPlanningValidator,
                                    PlanBlueprintRepository planBlueprintRepository,
                                    DepartmentRepository departmentRepository,
                                    ShiftPlanRotationService shiftPlanRotationService,
                                    ShiftPlanConstraintService shiftPlanConstraintService,
                                    ConcreteShiftPlanRepository concreteShiftPlanRepository,
                                    ScheduledShiftRepository scheduledShiftRepository,
                                    ShiftAssignmentAuditLogRepository shiftAssignmentAuditRepository) {
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftPlanningValidator = shiftPlanningValidator;
        this.shiftPlanConstraintService = shiftPlanConstraintService;
        this.shiftPlanRotationService = shiftPlanRotationService;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
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
        LocalDate startDate = timeService.nextMondayInMonth(dto.startDate()
            .orElseThrow(() -> new ConflictException("Start date is required")));

        concreteShiftPlanRepository.findByDepartmentName(department.getName()).stream()
            .min((a, b) -> b.getEndDate().compareTo(a.getEndDate())).ifPresent(concreteShiftPlan -> {
                if (concreteShiftPlan.getEndDate().isAfter(startDate)) {
                    throw new ConflictException("A concrete plan for this department already exists for the specified period.");
                }
            });

        // Initialisieren
        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        LocalDate endDate = startDate.plusWeeks(11);
        plan.setDepartment(department);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);

        Predicate<ApplicationUser> isNotSupervisor =
            u -> u.getRoles().stream().noneMatch(r -> r.getName().equals(Role.SUPERVISOR.name()));
        List<ApplicationUser> allUsers = department.getUsers().stream().toList();

        var justWorkers = allUsers.stream()
            .filter(isNotSupervisor)
            .toList();

        var rotatedPlan = shiftPlanRotationService.generateRotatingPlan(plan, blueprint, justWorkers);
        var planWithConstraints = shiftPlanConstraintService.applyConstraints(rotatedPlan);

        return planWithConstraints;
    }


    @Override
    public ConcreteShiftPlan getCurrentConcretePlan(String departmentName) {
        return concreteShiftPlanRepository.findByDepartmentName(departmentName)
            .stream()
            .max(Comparator.comparing(ConcreteShiftPlan::getStartDate))
            .orElseThrow(() -> new NotFoundException(("No current concrete plan found for department with ID: " + departmentName)));
    }

}
package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.mail.ScheduleAssignmentEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanRotationService;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.Role;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintAddShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftPlanningValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final MailService mailService;

    public ShiftPlanningServiceImpl(TimeService timeService,
                                    ShiftPlanningValidator shiftPlanningValidator,
                                    PlanBlueprintRepository planBlueprintRepository,
                                    DepartmentRepository departmentRepository,
                                    ShiftPlanRotationService shiftPlanRotationService,
                                    ConcreteShiftPlanRepository concreteShiftPlanRepository,
                                    ShiftPlanConstraintService shiftPlanConstraintService,
                                    ScheduledShiftRepository scheduledShiftRepository,
                                    MailService mailService) {
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftPlanningValidator = shiftPlanningValidator;
        this.shiftPlanConstraintService = shiftPlanConstraintService;
        this.shiftPlanRotationService = shiftPlanRotationService;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.scheduledShiftRepository = scheduledShiftRepository;
        this.mailService = mailService;
    }

    @Override
    public PlanBlueprintDto createPlanBlueprint(PlanBlueprintCreationDto createPlanBlueprintDto) {
        LOGGER.trace("createPlanBlueprint({})", createPlanBlueprintDto);

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
        LOGGER.trace("addShiftToPlan({})", addShiftDto);

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
    public ConcreteShiftPlan generateConcreteQuarterlyPlan(ConcretePlanGenerateDto dto) {
        LOGGER.trace("generateConcreteQuarterlyPlan({})", dto);

        PlanBlueprint blueprint = planBlueprintRepository.findById(dto.planBlueprintId())
            .orElseThrow(() -> new NotFoundException("Plan blueprint not found"));

        Department department = blueprint.getDepartment();
        LocalDate startDate = timeService.nextMondayInMonth(dto.startDate()
            .orElseThrow(() -> new ConflictException("Start date is required")));

        AtomicReference<List<ScheduledShift>> shiftsFromOlderShiftplan = new AtomicReference<>(new ArrayList<>());

        AtomicReference<ConcreteShiftPlan> concreteShiftPlanO = new AtomicReference<>(null);

        concreteShiftPlanRepository.findByDepartmentName(department.getName()).stream()
            .min((a, b) -> b.getEndDate().compareTo(a.getEndDate())).ifPresent(concreteShiftPlan -> {
                if (concreteShiftPlan.getEndDate().isAfter(startDate)) {
                    List<ScheduledShift> scheduledShifts = (concreteShiftPlan.getScheduledShifts().stream()
                        .filter(scheduledShift -> scheduledShift.getEnd().isBefore(startDate.atStartOfDay()))
                        .toList());

                    List<ScheduledShift> newScheduledShifts = new ArrayList<>(scheduledShifts.size());
                    for (ScheduledShift scheduledShift : scheduledShifts) {
                        ScheduledShift newScheduledShift = new ScheduledShift();
                        newScheduledShift.setStart(scheduledShift.getStart());
                        newScheduledShift.setEnd(scheduledShift.getEnd());
                        newScheduledShift.setDescription(scheduledShift.getDescription());
                        newScheduledShift.setManpower(scheduledShift.getManpower());

                        ArrayList<ScheduledShiftAssignment> scheduledShiftAssignments = new ArrayList<>();
                        for (ScheduledShiftAssignment scheduledShiftAssignment : scheduledShift.getAssignments()) {
                            ScheduledShiftAssignment newAssignment = new ScheduledShiftAssignment();
                            newAssignment.setUser(scheduledShiftAssignment.getUser());
                            newAssignment.setScheduledShift(newScheduledShift);
                            scheduledShiftAssignments.add(newAssignment);
                        }

                        newScheduledShift.setAssignments(Set.copyOf(scheduledShiftAssignments));
                        newScheduledShifts.add(newScheduledShift);
                    }

                    concreteShiftPlan.setOverwritten(true);
                    shiftsFromOlderShiftplan.set(newScheduledShifts);
                }
            });

        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        LocalDate endDate = startDate.plusWeeks(12).minusDays(1);
        plan.setDepartment(department);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);

        Predicate<ApplicationUser> isEmployee =
            u -> u.getRoles().stream()
                .anyMatch(r -> r.getName().equals(Role.EMPLOYEE.name()));
        List<ApplicationUser> allUsers = department.getUsers().stream().toList();

        var justWorkers = allUsers.stream()
            .filter(isEmployee)
            .toList();

        var rotatedPlan = shiftPlanRotationService.generateRotatingPlan(plan, blueprint, justWorkers);
        var planWithConstraints = shiftPlanConstraintService.applyConstraints(rotatedPlan);
        List<ScheduledShift> shiftsFromOldPlan = shiftsFromOlderShiftplan.get();
        for (ScheduledShift scheduledShift : shiftsFromOldPlan) {
            scheduledShift.setPlan(planWithConstraints);
        }
        planWithConstraints.addScheduledShifts(shiftsFromOldPlan);

        // Get all distinct users from the plan (assigned to any shift)
        Set<ApplicationUser> notifiedUsers = planWithConstraints.getScheduledShifts().stream()
            .flatMap(shift -> shift.getAssignments().stream())
            .map(assignment -> assignment.getUser())
            .collect(Collectors.toSet());

        for (ApplicationUser user : notifiedUsers) {
            try {
                ScheduleAssignmentEmailDto emailDto = new ScheduleAssignmentEmailDto(
                    user.getEmail(),
                    user.getFirstName(),
                    startDate,
                    endDate,
                    department.getName()
                );
                mailService.sendScheduleAssignmentNotification(emailDto);
                LOGGER.info("Sent schedule assignment mail to {}", user.getEmail());
            } catch (Exception e) {
                LOGGER.warn("Failed to send schedule mail to {}: {}", user.getEmail(), e.getMessage(), e);
            }
        }

        return planWithConstraints;
    }


    @Override
    public ConcreteShiftPlan getCurrentConcretePlan(String departmentName) {
        LOGGER.trace("getCurrentConcretePlan({})", departmentName);

        return concreteShiftPlanRepository.findByDepartmentName(departmentName)
            .stream()
            .max(Comparator.comparing(ConcreteShiftPlan::getStartDate))
            .orElseThrow(() -> new NotFoundException(("No current concrete plan found for department with ID: " + departmentName)));
    }

    @Override
    public Optional<DepartmentNameDto> getDeparmentNameForShiftBlueprint(Long id) {
        LOGGER.trace("getDeparmentNameForShiftBlueprint({})", id);

        return planBlueprintRepository.findById(id)
            .map(p -> new DepartmentNameDto(p.getDepartment().getName()));
    }

    @Override
    public List<ConcreteShiftPlan> getAllNotOverridenPlans(String departmentName) {
        LOGGER.trace("getAllPlans({})", departmentName);

        return concreteShiftPlanRepository.findByDepartmentName(departmentName).stream()
            .filter(c -> !c.isOverwritten()).toList();
    }

}
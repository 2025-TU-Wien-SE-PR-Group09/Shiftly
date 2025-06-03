package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftId;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ScheduledShiftDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDetailDto;
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
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ShiftPlanningServiceImpl implements ShiftPlanningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PlanBlueprintRepository planBlueprintRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeService timeService;
    private final ShiftPlanningValidator shiftPlanningValidator;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final UserRepository userRepository;


    public ShiftPlanningServiceImpl(TimeService timeService,
                                    ShiftPlanningValidator shiftPlanningValidator,
                                    PlanBlueprintRepository planBlueprintRepository,
                                    DepartmentRepository departmentRepository,
                                    ConcreteShiftPlanRepository concreteShiftPlanRepository,
                                    ScheduledShiftRepository scheduledShiftRepository,
                                    UserRepository userRepository) {
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftPlanningValidator = shiftPlanningValidator;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.scheduledShiftRepository = scheduledShiftRepository;
        this.userRepository = userRepository;
    }


    @Override
    public PlanBlueprintDto createPlanBlueprint(PlanBlueprintCreationDto createPlanBlueprintDto) {
        var dept = departmentRepository.findById(createPlanBlueprintDto.departmentId())
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

        plan = this.planBlueprintRepository.save(plan);
        return ShiftPlanningMapper.Plans.fromEntity(plan);
    }

    @Override
    public PlanBlueprintDto addShiftToPlan(PlanBlueprintAddShiftDto addShiftDto) {
        var plan = this.planBlueprintRepository.findById(addShiftDto.planId())
            .orElseThrow(() -> new NotFoundException("Plan not found!"));

        //TODO: validation
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

        newShifts.forEach(plan::addShiftBlueprint);
        plan = this.planBlueprintRepository.save(plan);

        return ShiftPlanningMapper.Plans.fromEntity(plan);
    }

    @Override
    public ConcreteShiftPlan generateConcreteQuarterlyPlan(ConcretePlanGenerateDto concretePlanGenerateDto) {
        PlanBlueprint blueprint = planBlueprintRepository.findById(concretePlanGenerateDto.planBlueprintId())
            .orElseThrow(() -> new NotFoundException("Plan blueprint not found"));

        Department department = blueprint.getDepartment();

        var month = concretePlanGenerateDto.startDate().orElseThrow(() -> new ConflictException("Start date is required"));
        LocalDate startDate = timeService.nextMondayInMonth(month);

        concreteShiftPlanRepository.findByDepartmentId(department.getId()).stream()
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

        ApplicationUser adminUser = userRepository.findByEmail("admin@shyft.local")
            .orElseThrow(() -> new NotFoundException("Admin user not found"));

        List<ShiftBlueprint> shiftBlueprints = blueprint.getShifts();
        List<ScheduledShift> scheduledShifts = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            LocalDate weekStart = startDate.plusWeeks(i);
            int calendarWeek = weekStart.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            int year = weekStart.getYear();

            for (ShiftBlueprint shiftBlueprint : shiftBlueprints) {
                List<ShiftWeekBlueprint> shiftWeekBlueprints = shiftBlueprint.getShiftWeeks();
                if (shiftWeekBlueprints.isEmpty()) {
                    continue;
                }

                ShiftWeekBlueprint templateWeek = shiftWeekBlueprints.get(i % shiftWeekBlueprints.size());

                ScheduledShiftId shiftId = new ScheduledShiftId(
                    department.getId(),
                    calendarWeek,
                    year,
                    shiftBlueprint.getId()
                );

                ScheduledShift scheduled = new ScheduledShift.Builder()
                    .withId(shiftId)
                    .withDepartment(department)
                    .withShiftBlueprint(shiftBlueprint)
                    .withPlan(plan)
                    .withWeekStartDate(weekStart)
                    .build();

                // Beispiel: Jede 2. Schicht bekommt eine Admin-Zuweisung
                if ((i + shiftBlueprint.getId()) % 3 == 0) {
                    ScheduledShiftAssignment assignment = new ScheduledShiftAssignment.Builder()
                        .withShift(scheduled)
                        .withUser(adminUser)
                        .build();

                    scheduled.addAssignment(assignment);
                }

                scheduledShifts.add(scheduled);
            }
        }

        plan.addScheduledShifts(scheduledShifts);
        return concreteShiftPlanRepository.save(plan);
    }

    @Override
    public ConcreteShiftPlan getCurrentConcretePlan(Long departmentId) {
        return concreteShiftPlanRepository.findByDepartmentId(departmentId)
            .stream()
            .max(Comparator.comparing(ConcreteShiftPlan::getStartDate))
            .orElseThrow(() -> new NotFoundException(("No current concrete plan found for department with ID: " + departmentId)));
    }


    public List<ScheduledShiftDetailDto> getCurrentConcretePlanDebug(Long departmentId) {
        List<ScheduledShift> scheduledShifts = scheduledShiftRepository.findByDepartmentId(departmentId);

        return scheduledShifts.stream()
            .map(shift -> {
                List<ShiftDayDetailDto> days = shift.getShift().getShiftWeeks().stream()
                    .flatMap(week -> week.getDays().stream())
                    .map(day -> new ShiftDayDetailDto(
                        day.getDay(),
                        day.getStartTime(),
                        day.getDuration(),
                        shift.getAssignments().stream()
                            .map(a -> a.getUser().getEmail())
                            .toList()
                    ))
                    .toList();

                return new ScheduledShiftDetailDto(
                    shift.getShift().getDescription(),
                    shift.getWeekStartDate(),
                    days
                );
            })
            .toList();
    }
}


package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftDayBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftWeekBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ScheduledShiftDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ShiftDayDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.CreateShiftBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftWeekValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
public class ShiftPlanningServiceImpl implements ShiftPlanningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ShiftDayBlueprintRepository shiftDayBlueprintRepository;
    private final ShiftWeekBlueprintRepository shiftWeekBlueprintRepository;
    private final ShiftBlueprintRepository shiftBlueprintRepository;
    private final PlanBlueprintRepository planBlueprintRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeService timeService;
    private final ShiftWeekValidator shiftWeekValidator;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final UserRepository userRepository;


    public ShiftPlanningServiceImpl(ShiftDayBlueprintRepository shiftDayBlueprintRepository,
                                    ShiftWeekBlueprintRepository shiftWeekBlueprintRepository,
                                    ShiftBlueprintRepository shiftBlueprintRepository,
                                    TimeService timeService,
                                    ShiftWeekValidator shiftWeekValidator,
                                    PlanBlueprintRepository planBlueprintRepository,
                                    DepartmentRepository departmentRepository,
                                    ConcreteShiftPlanRepository concreteShiftPlanRepository,
                                    ScheduledShiftRepository scheduledShiftRepository,
                                    UserRepository userRepository) {
        this.shiftDayBlueprintRepository = shiftDayBlueprintRepository;
        this.shiftWeekBlueprintRepository = shiftWeekBlueprintRepository;
        this.shiftBlueprintRepository = shiftBlueprintRepository;
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftWeekValidator = shiftWeekValidator;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.scheduledShiftRepository = scheduledShiftRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PlanBlueprintDto createPlanBlueprint(Long departmentId, List<Long> shiftIds) {

        var dept = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        List<ShiftBlueprint> shiftBlueprints = this.shiftBlueprintRepository.findAllById(shiftIds);
        if (shiftBlueprints.isEmpty()) {
            throw new NotFoundException("Shifts not found!");
        }

        var plan = new PlanBlueprint(timeService.nextMonday(), dept);
        plan.setShifts(new HashSet<>(shiftBlueprints));

        return ShiftPlanningMapper.Plans.fromEntity(this.planBlueprintRepository.save(plan));
    }

    @Override
    public PlanBlueprintDto addShiftToCurrentPlan(Long departmentId, ShiftBlueprintDto shift) {

        var plan = this.departmentRepository.findById(departmentId)
            .orElseThrow(() -> new NotFoundException("No plan found for department!"))
            .getPlans().stream().min(Comparator.comparing(p -> p.getId().getStartDate()))
            .orElseThrow(() -> new NotFoundException("No plans found for department!"));

        var shiftEntity = this.shiftBlueprintRepository.findById(shift.id())
            .orElseThrow(() -> new NotFoundException("Shift not found!"));

        plan.getShifts().add(shiftEntity);
        return ShiftPlanningMapper.Plans.fromEntity(this.planBlueprintRepository.save(plan));
    }


    @Override
    public ShiftBlueprintDto createShiftBlueprint(CreateShiftBlueprintDto createShiftBlueprintDto) {

        if (createShiftBlueprintDto.manPower() <= 0) {
            throw new ConflictException("At least one worker must work at this shift!");
        }

        ShiftBlueprint savedEntity = this.shiftBlueprintRepository
            .save(new ShiftBlueprint(createShiftBlueprintDto.description(), createShiftBlueprintDto.manPower()));

        return ShiftPlanningMapper.Shifts.fromEntity(savedEntity);
    }

    @Override
    public ShiftBlueprintDto addWeeksToShift(Long shiftId, List<ShiftWeekBlueprintDto> shiftWeekBlueprints) {
        var shift = this.shiftBlueprintRepository.findById(shiftId)
            .orElseThrow(() -> new NotFoundException(String.format("Shift with id %d not found!", shiftId)));

        shiftWeekBlueprints.forEach(shiftWeekBlueprint -> {
            shiftWeekValidator.validateWorkingHours(shiftWeekBlueprint).ifPresent(errors -> {
                throw new ConflictException(errors);
            });
        });

        int weekIndex = shift.getShiftWeeks().size();
        for (var week : shiftWeekBlueprints) {
            var shiftWeek = this.shiftWeekBlueprintRepository.save(new ShiftWeekBlueprint(weekIndex, shift));
            this.shiftWeekBlueprintRepository.save(shiftWeek);
            weekIndex++;

            var shiftDays = week.shiftDays().stream()
                .map(swd -> new ShiftDayBlueprint(
                    swd.day(),
                    swd.startTime(),
                    swd.duration(),
                    shiftWeek)).toList();

            var savedShiftDays = this.shiftDayBlueprintRepository.saveAll(shiftDays);
            shiftWeek.setDays(savedShiftDays);
            this.shiftWeekBlueprintRepository.save(shiftWeek);
            shift.getShiftWeeks().add(shiftWeek);
        }

        var savedShift = this.shiftBlueprintRepository.save(shift);
        return ShiftPlanningMapper.Shifts.fromEntity(savedShift);
    }

    @Override
    public ConcreteShiftPlan generateConcreteQuarterlyPlan(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new NotFoundException("Department not found"));


        LocalDate startDate = timeService.nextMonday();
        LocalDate endDate = startDate.plusWeeks(11); // 12 Wochen

        ConcreteShiftPlan plan = new ConcreteShiftPlan();
        plan.setDepartment(department);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);

        List<ScheduledShift> scheduledShifts = new ArrayList<>();
        //TODO: not hardcode admin user
        ApplicationUser adminUser = userRepository.findByEmail("admin@shyft.local")
            .orElseThrow(() -> new NotFoundException("Admin user not found"));

        PlanBlueprint blueprint = planBlueprintRepository.findByDepartment(department)
            .orElseThrow(() -> new NotFoundException("No template plan found for department"));
        for (ShiftBlueprint shiftBlueprint : blueprint.getShifts()) {
            List<ShiftWeekBlueprint> shiftWeekBlueprints = shiftBlueprint.getShiftWeeks();
            if (shiftWeekBlueprints.isEmpty()) {
                continue;
            }

            for (int i = 0; i < 12; i++) {
                LocalDate weekStart = startDate.plusWeeks(i);
                int calendarWeek = weekStart.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                int year = weekStart.getYear();

                ShiftWeekBlueprint templateWeek = shiftWeekBlueprints.get(i % shiftWeekBlueprints.size());

                ScheduledShiftId shiftId = new ScheduledShiftId();
                shiftId.setDepartmentId(department.getId());
                shiftId.setCalendarWeek(calendarWeek);
                shiftId.setCalendarYear(year);
                shiftId.setShiftBlueprintId(shiftBlueprint.getId());

                ScheduledShift scheduled = new ScheduledShift();
                scheduled.setId(shiftId);
                scheduled.setDepartment(department);
                scheduled.setShift(shiftBlueprint);
                scheduled.setWeekStartDate(weekStart);
                scheduled.setPlan(plan);

                if (i % 3 == 0) {
                    ScheduledShiftAssignment assignment = new ScheduledShiftAssignment();
                    assignment.setUser(adminUser);
                    assignment.setScheduledShift(scheduled);
                    scheduled.getAssignments().add(assignment);
                }

                scheduledShifts.add(scheduled);
            }
        }

        plan.setScheduledShifts(scheduledShifts);
        return concreteShiftPlanRepository.save(plan);
    }

    public List<ScheduledShiftDetailDto> getCurrentConcretePlan(Long departmentId) {
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


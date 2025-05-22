package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftDayRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftWeekRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.Shift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDay;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeek;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftAssignment;
import at.ac.tuwien.sepr.groupphase.backend.entity.ScheduledShiftId;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ScheduledShiftDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ShiftDayDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.CreateShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftWeekValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class ShiftPlanningServiceImpl implements ShiftPlanningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ShiftDayRepository shiftDayRepository;
    private final ShiftWeekRepository shiftWeekRepository;
    private final ShiftRepository shiftRepository;
    private final PlanBlueprintRepository planBlueprintRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeService timeService;
    private final ShiftWeekValidator shiftWeekValidator;
    private final ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final UserRepository userRepository;


    public ShiftPlanningServiceImpl(ShiftDayRepository shiftDayRepository,
                                    ShiftWeekRepository shiftWeekRepository,
                                    ShiftRepository shiftRepository,
                                    TimeService timeService,
                                    ShiftWeekValidator shiftWeekValidator,
                                    PlanBlueprintRepository planBlueprintRepository,
                                    DepartmentRepository departmentRepository,
                                    ConcreteShiftPlanRepository concreteShiftPlanRepository,
                                    ScheduledShiftRepository scheduledShiftRepository,
                                    UserRepository userRepository) {
        this.shiftDayRepository = shiftDayRepository;
        this.shiftWeekRepository = shiftWeekRepository;
        this.shiftRepository = shiftRepository;
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftWeekValidator = shiftWeekValidator;
        this.concreteShiftPlanRepository = concreteShiftPlanRepository;
        this.scheduledShiftRepository = scheduledShiftRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PlanBlueprintDto createPlan(Long departmentId, List<Long> shiftIds) {

        var dept = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        List<Shift> shifts = this.shiftRepository.findAllById(shiftIds);
        if (shifts.isEmpty()) {
            throw new NotFoundException("Shifts not found!");
        }

        var plan = new PlanBlueprint(timeService.nextMonday(), dept);
        plan.setShifts(new HashSet<>(shifts));

        return ShiftPlanningMapper.Plans.fromEntity(this.planBlueprintRepository.save(plan));
    }

    @Override
    public ShiftDto createShift(CreateShiftDto createShiftDto) {

        if (createShiftDto.manPower() <= 0) {
            throw new ConflictException("At least one worker must work at this shift!");
        }

        Shift savedEntity = this.shiftRepository
            .save(new Shift(createShiftDto.description(), createShiftDto.manPower()));

        return ShiftPlanningMapper.Shifts.fromEntity(savedEntity);
    }

    @Override
    public ShiftDto addWeekToShift(Long shiftId, ShiftWeekDto shiftWeekDto) {
        var shift = this.shiftRepository.findById(shiftId)
            .orElseThrow(() -> new NotFoundException(String.format("Shift with id %d not found!", shiftId)));

        shiftWeekValidator.validateWorkingHours(shiftWeekDto).ifPresent(errors -> {
            throw new ConflictException(errors);
        });

        int weekIndex = shift.getShiftWeeks().size();

        var shiftWeek = this.shiftWeekRepository.save(new ShiftWeek(weekIndex, shift));
        this.shiftWeekRepository.save(shiftWeek);

        var shiftDays = shiftWeekDto.shiftDays().stream()
            .map(swd -> new ShiftDay(
                swd.day(),
                swd.startTime(),
                swd.duration(),
                shiftWeek)).toList();
        var savedShiftDays = this.shiftDayRepository.saveAll(shiftDays);

        shiftWeek.setDays(savedShiftDays);
        this.shiftWeekRepository.save(shiftWeek);

        shift.getShiftWeeks().add(shiftWeek);
        var savedShift = this.shiftRepository.save(shift);
        return ShiftPlanningMapper.Shifts.fromEntity(savedShift);
    }

    @Override
    public ConcreteShiftPlan generateQuarterlyPlan(Long departmentId) {

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
        for (Shift shift : blueprint.getShifts()) {
            List<ShiftWeek> shiftWeeks = shift.getShiftWeeks();
            if (shiftWeeks.isEmpty()) {
                continue;
            }

            for (int i = 0; i < 12; i++) {
                LocalDate weekStart = startDate.plusWeeks(i);
                int calendarWeek = weekStart.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                int year = weekStart.getYear();

                ShiftWeek templateWeek = shiftWeeks.get(i % shiftWeeks.size());

                ScheduledShiftId shiftId = new ScheduledShiftId();
                shiftId.setDepartmentId(department.getId());
                shiftId.setCalendarWeek(calendarWeek);
                shiftId.setCalendarYear(year);
                shiftId.setShiftId(shift.getId());

                ScheduledShift scheduled = new ScheduledShift();
                scheduled.setId(shiftId);
                scheduled.setDepartment(department);
                scheduled.setShift(shift);
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

    public List<ScheduledShiftDetailDto> getDetailedConcretePlan(Long departmentId) {
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


package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Shift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDay;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeek;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftDayRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftWeekRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.CreateShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftWeekValidator;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class ShiftPlanningServiceImpl implements ShiftPlanningService {

    private final ShiftDayRepository shiftDayRepository;
    private final ShiftWeekRepository shiftWeekRepository;
    private final ShiftRepository shiftRepository;
    private final PlanBlueprintRepository planBlueprintRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeService timeService;
    private final ShiftWeekValidator shiftWeekValidator;

    public ShiftPlanningServiceImpl(ShiftDayRepository shiftDayRepository,
                                    ShiftWeekRepository shiftWeekRepository,
                                    ShiftRepository shiftRepository,
                                    TimeService timeService,
                                    ShiftWeekValidator shiftWeekValidator,
                                    PlanBlueprintRepository planBlueprintRepository, DepartmentRepository departmentRepository) {
        this.shiftDayRepository = shiftDayRepository;
        this.shiftWeekRepository = shiftWeekRepository;
        this.shiftRepository = shiftRepository;
        this.planBlueprintRepository = planBlueprintRepository;
        this.timeService = timeService;
        this.departmentRepository = departmentRepository;
        this.shiftWeekValidator = shiftWeekValidator;
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
}
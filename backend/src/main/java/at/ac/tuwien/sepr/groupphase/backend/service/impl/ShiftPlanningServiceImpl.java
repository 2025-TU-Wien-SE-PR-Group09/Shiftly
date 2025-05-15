package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Plan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Shift;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDay;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeek;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftDayRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftWeekRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.List;

@Service
public class ShiftPlanningServiceImpl implements ShiftPlanningService {

    private final ShiftDayRepository shiftDayRepository;
    private final ShiftWeekRepository shiftWeekRepository;
    private final ShiftRepository shiftRepository;
    private final PlanRepository planRepository;
    private final DepartmentRepository departmentRepository;


    public ShiftPlanningServiceImpl(ShiftDayRepository shiftDayRepository,
                                    ShiftWeekRepository shiftWeekRepository,
                                    ShiftRepository shiftRepository,
                                    PlanRepository planRepository, DepartmentRepository departmentRepository) {
        this.shiftDayRepository = shiftDayRepository;
        this.shiftWeekRepository = shiftWeekRepository;
        this.shiftRepository = shiftRepository;
        this.planRepository = planRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public PlanDto createPlan(Long departmentId, List<ShiftDto> shifts) {

        var dept = departmentRepository.findById(departmentId).orElseThrow(() -> new NotFoundException("Department not found!"));
        var plan = new Plan(Month.MAY, dept);

        for (ShiftDto shift : shifts) {

            var s = new Shift(shift.description(), shift.manPower());

            shiftRepository.save(s);

            for (int i = 0; i < shift.shiftWeeks().size(); i++) {
                var shiftWeek = shift.shiftWeeks().get(i);
                var savedShiftWeek = new ShiftWeek(i, s);

                var shiftDays = shiftWeek.shiftDays().stream()
                    .map(shiftDay -> new ShiftDay(shiftDay.day(), shiftDay.startTime(), shiftDay.duration(), savedShiftWeek));
                var savedShiftDays = shiftDayRepository.saveAll(shiftDays.toList());

                savedShiftWeek.setDays(savedShiftDays);
                shiftWeekRepository.save(savedShiftWeek);
            }
            plan.getShifts().add(s);
        }
        planRepository.save(plan);

        return ShiftPlanningMapper.fromEntity(plan);
    }
}

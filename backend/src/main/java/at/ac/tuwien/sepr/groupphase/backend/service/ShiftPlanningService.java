package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDto;

import java.util.List;

public interface ShiftPlanningService {

    PlanDto createPlan(Long departmentId, List<ShiftDto> shifts);
}

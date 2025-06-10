package at.ac.tuwien.sepr.groupphase.backend.service.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;

import java.util.List;

public interface ShiftPlanRotationService {

    ConcreteShiftPlan generateRotatingPlan(ConcreteShiftPlan concreteShiftPlan, PlanBlueprint planBlueprint, List<ApplicationUser> employees);
}

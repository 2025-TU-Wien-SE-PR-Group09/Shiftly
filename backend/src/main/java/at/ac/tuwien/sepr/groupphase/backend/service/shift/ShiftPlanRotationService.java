package at.ac.tuwien.sepr.groupphase.backend.service.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;

import java.util.List;

public interface ShiftPlanRotationService {
    /**
     * Generates a rotating shift plan based on the provided concrete shift plan and plan blueprint.
     *
     * @param concreteShiftPlan the concrete shift plan to be rotated
     * @param planBlueprint the plan blueprint that defines the structure of the shift plan
     * @param employees the list of employees to be assigned to the shifts
     * @return a new ConcreteShiftPlan with the rotated shifts
     */
    ConcreteShiftPlan generateRotatingPlan(ConcreteShiftPlan concreteShiftPlan, PlanBlueprint planBlueprint, List<ApplicationUser> employees);
}

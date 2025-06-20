package at.ac.tuwien.sepr.groupphase.backend.service.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;

public interface ShiftPlanConstraintService {
    /**
     * Applies constraints to the given concrete shift plan.
     *
     * @param concreteShiftPlan the concrete shift plan to which constraints will be applied
     * @return the modified concrete shift plan with constraints applied
     */
    ConcreteShiftPlan applyConstraints(ConcreteShiftPlan concreteShiftPlan);

}

package at.ac.tuwien.sepr.groupphase.backend.service.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;

public interface ShiftPlanConstraintService {

    ConcreteShiftPlan applyConstraints(ConcreteShiftPlan concreteShiftPlan);

}

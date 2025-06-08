package at.ac.tuwien.sepr.groupphase.backend.service.impl.shift;

import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanConstraintService;
import org.springframework.stereotype.Service;

@Service
public class ShiftPlanConstraintServiceImpl implements ShiftPlanConstraintService {
    @Override
    public ConcreteShiftPlan applyConstraints(ConcreteShiftPlan concreteShiftPlan) {
        return null;
    }
}

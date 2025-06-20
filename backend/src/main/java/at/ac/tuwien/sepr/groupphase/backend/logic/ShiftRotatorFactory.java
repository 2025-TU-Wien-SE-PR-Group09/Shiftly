package at.ac.tuwien.sepr.groupphase.backend.logic;

import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;

@FunctionalInterface
public interface ShiftRotatorFactory {
    ShiftRotator create(PlanBlueprint blueprint);
}

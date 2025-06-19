package at.ac.tuwien.sepr.groupphase.backend.logic;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;

import java.util.List;

@FunctionalInterface
public interface InitialShiftAssignmentStrategy {
    void assign(List<RotatingShiftSlot> slots, List<ApplicationUser> users);
}

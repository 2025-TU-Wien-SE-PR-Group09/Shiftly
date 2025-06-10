package at.ac.tuwien.sepr.groupphase.backend.entity;

public enum ShiftAssignmentTrigger {

    // Automatic processes
    INITIAL_ASSIGNMENT_ALGORITHM,
    REASSIGNMENT_DUE_TO_VACATION,
    SHIFT_SWAP_ALGORITHM,

    // Manual by roles
    MANUAL_BY_ADMIN,
    MANUAL_BY_SUPERVISOR,

}
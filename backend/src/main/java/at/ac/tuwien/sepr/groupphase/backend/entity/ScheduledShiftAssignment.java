package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "scheduled_shift_assignment")
public class ScheduledShiftAssignment {

    @EmbeddedId
    private ScheduledShiftAssignmentId id = new ScheduledShiftAssignmentId();

    @ManyToOne(optional = false)
    @MapsId("scheduledShiftId")
    @JoinColumns({
        @JoinColumn(name = "department_id", referencedColumnName = "department_id"),
        @JoinColumn(name = "calendar_week", referencedColumnName = "calendar_week"),
        @JoinColumn(name = "calendar_year", referencedColumnName = "calendar_year"),
        @JoinColumn(name = "shift_blueprint_id", referencedColumnName = "shift_blueprint_id")
    })
    private ScheduledShift scheduledShift;

    @ManyToOne(optional = false)
    @MapsId("userEmail")
    @JoinColumn(name = "user_email", referencedColumnName = "email")
    private ApplicationUser user;

    public ScheduledShiftAssignment() {
    }

    public ScheduledShiftAssignment(ScheduledShift scheduledShift, ApplicationUser user) {
        this.scheduledShift = scheduledShift;
        this.user = user;
        this.id = new ScheduledShiftAssignmentId(scheduledShift.getId(), user.getEmail());
    }

    public ScheduledShiftAssignmentId getId() {
        return id;
    }

    public void setId(ScheduledShiftAssignmentId id) {
        this.id = id;
    }

    public ScheduledShift getScheduledShift() {
        return scheduledShift;
    }

    public void setScheduledShift(ScheduledShift scheduledShift) {
        this.scheduledShift = scheduledShift;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }
}
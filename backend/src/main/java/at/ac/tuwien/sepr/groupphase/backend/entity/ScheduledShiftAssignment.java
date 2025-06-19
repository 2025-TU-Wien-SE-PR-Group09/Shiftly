package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "scheduled_shift_assignment", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"scheduled_shift_id", "user_email"})
})
public class ScheduledShiftAssignment {

    @EmbeddedId
    private ScheduledShiftAssignmentId id = new ScheduledShiftAssignmentId();

    @ManyToOne(optional = false)
    @MapsId("scheduledShiftId")
    @JoinColumn(name = "scheduled_shift_id", referencedColumnName = "id")
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

    public void setUserEmail(String email) {
        this.id.setUserEmail(email);
    }


    public static class Builder {
        private final ScheduledShiftAssignment assignment = new ScheduledShiftAssignment();

        public Builder withId(ScheduledShiftAssignmentId id) {
            assignment.setId(id);
            return this;
        }

        public Builder withUser(ApplicationUser user) {
            assignment.setUser(user);
            return this;
        }

        public Builder withShift(ScheduledShift shift) {
            assignment.setScheduledShift(shift);
            shift.getAssignments().add(assignment);
            return this;
        }

        public ScheduledShiftAssignment build() {
            return assignment;
        }
    }

    @Override
    public String toString() {
        return "ScheduledShiftAssignment{"
            + "id=" + id
            + ", scheduledShift=" + scheduledShift
            + ", user=" + user
            + '}';
    }
}
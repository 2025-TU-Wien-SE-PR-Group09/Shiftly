package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScheduledShiftAssignmentId implements Serializable {

    @Column(name = "scheduled_shift_id")
    private Long scheduledShiftId;

    @Column(name = "user_email")
    private String userEmail;

    public ScheduledShiftAssignmentId() {
    }

    public ScheduledShiftAssignmentId(Long scheduledShiftId, String userEmail) {
        this.scheduledShiftId = scheduledShiftId;
        this.userEmail = userEmail;
    }

    public Long getScheduledShiftId() {
        return scheduledShiftId;
    }

    public void setScheduledShiftId(Long scheduledShiftId) {
        this.scheduledShiftId = scheduledShiftId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScheduledShiftAssignmentId that)) {
            return false;
        }
        return Objects.equals(scheduledShiftId, that.scheduledShiftId)
                &&
                Objects.equals(userEmail, that.userEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledShiftId, userEmail);
    }
}
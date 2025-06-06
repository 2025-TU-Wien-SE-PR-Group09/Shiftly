package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift_assignment_audit_log")
public class ShiftAssignmentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ShiftAssignmentTrigger trigger;

    @Column(length = 100)
    private String performedByUserEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_user_email", referencedColumnName = "email")
    private ApplicationUser assignedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheduled_shift_id")
    private ScheduledShift scheduledShift;

    @Column(length = 500)
    private String reason;

    // Constructors
    public ShiftAssignmentAuditLog() {
    }

    public ShiftAssignmentAuditLog(LocalDateTime timestamp,
            ShiftAssignmentTrigger trigger,
            String performedByUserEmail,
            ApplicationUser assignedUser,
            ScheduledShift scheduledShift,
            String reason) {
        this.timestamp = timestamp;
        this.trigger = trigger;
        this.performedByUserEmail = performedByUserEmail;
        this.assignedUser = assignedUser;
        this.scheduledShift = scheduledShift;
        this.reason = reason;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ShiftAssignmentTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(ShiftAssignmentTrigger trigger) {
        this.trigger = trigger;
    }

    public String getPerformedByUserEmail() {
        return performedByUserEmail;
    }

    public void setPerformedByUserEmail(String performedByUserEmail) {
        this.performedByUserEmail = performedByUserEmail;
    }

    public ApplicationUser getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(ApplicationUser assignedUser) {
        this.assignedUser = assignedUser;
    }

    public ScheduledShift getScheduledShift() {
        return scheduledShift;
    }

    public void setScheduledShift(ScheduledShift scheduledShift) {
        this.scheduledShift = scheduledShift;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static class Builder {
        private final ShiftAssignmentAuditLog log = new ShiftAssignmentAuditLog();

        public Builder withTrigger(ShiftAssignmentTrigger trigger) {
            log.setTrigger(trigger);
            return this;
        }

        public Builder withTimestamp(LocalDateTime timestamp) {
            log.setTimestamp(timestamp);
            return this;
        }

        public Builder withShift(ScheduledShift scheduledShift) {
            log.setScheduledShift(scheduledShift);
            return this;
        }

        public Builder withWorker(ApplicationUser worker) {
            log.setAssignedUser(worker);
            return this;
        }

        public Builder manuelAssignment(String reason, String performedBy, ShiftAssignmentTrigger trigger) {
            log.setReason(reason);
            log.setPerformedByUserEmail(performedBy);
            log.setTrigger(trigger);
            return this;

        }

        public ShiftAssignmentAuditLog build() {
            return log;
        }
    }
}

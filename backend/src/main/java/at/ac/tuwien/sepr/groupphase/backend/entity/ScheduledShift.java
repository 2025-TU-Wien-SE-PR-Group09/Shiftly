package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "scheduled_shift")
public class ScheduledShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ConcreteShiftPlan plan;

    @Column(name = "shift_start", nullable = false)
    private LocalDateTime start;

    @Column(name = "shift_end", nullable = false)
    private LocalDateTime end;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int manpower;

    @OneToMany(mappedBy = "scheduledShift", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScheduledShiftAssignment> assignments = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartment() {
        return this.getPlan().getDepartment();
    }

    public ConcreteShiftPlan getPlan() {
        return plan;
    }

    public void setPlan(ConcreteShiftPlan plan) {
        this.plan = plan;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getDescription() {
        return description;
    }

    public Set<ScheduledShiftAssignment> getAssignments() {
        return assignments;
    }

    public void addAssignment(ScheduledShiftAssignment assignment) {
        assignments.add(assignment);
        assignment.setScheduledShift(this);
    }

    public int getManpower() {
        return manpower;
    }

    public void setManpower(int manpower) {
        this.manpower = manpower;
    }

    public void setAssignments(Set<ScheduledShiftAssignment> assignments) {
        this.assignments = assignments;
    }

    public static class Builder {
        private final ScheduledShift shift = new ScheduledShift();

        public Builder withId(Long id) {
            shift.setId(id);
            return this;
        }

        public Builder withEnd(LocalDateTime end) {
            shift.setEnd(end);
            return this;
        }

        public Builder withStart(LocalDateTime start) {
            shift.setStart(start);
            return this;
        }

        public Builder withManpower(int manpower) {
            shift.setManpower(manpower);
            return this;
        }

        public Builder withDescription(String description) {
            shift.setDescription(description);
            return this;
        }

        public Builder withPlan(ConcreteShiftPlan plan) {
            shift.setPlan(plan);
            return this;
        }

        public Builder addAssignment(ScheduledShiftAssignment assignment) {
            shift.addAssignment(assignment);
            return this;
        }

        public ScheduledShift build() {
            return shift;
        }
    }
}
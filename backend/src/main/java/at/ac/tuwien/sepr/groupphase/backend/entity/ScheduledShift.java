package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.MapsId;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ScheduledShift {

    @EmbeddedId
    private ScheduledShiftId id;

    @ManyToOne(optional = false)
    @MapsId("departmentId")
    private Department department;

    @ManyToOne(optional = false)
    @MapsId("shiftId")
    private Shift shift;

    private LocalDate weekStartDate;

    @ManyToOne(optional = false)
    private ConcreteShiftPlan plan;

    @OneToMany(mappedBy = "scheduledShift", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScheduledShiftAssignment> assignments = new HashSet<>();

    public ScheduledShiftId getId() {
        return id;
    }

    public void setId(ScheduledShiftId id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public ConcreteShiftPlan getPlan() {
        return plan;
    }

    public void setPlan(ConcreteShiftPlan plan) {
        this.plan = plan;
    }

    public Set<ScheduledShiftAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<ScheduledShiftAssignment> assignments) {
        this.assignments = assignments;
    }
}
package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a blueprint for a plan, which includes the department and the shifts
 * associated with that plan.
 */
@Entity
public class PlanBlueprint {

    @EmbeddedId
    private PlanId id;

    @MapsId("departmentId")
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany
    @JoinTable(name = "plan_shift", joinColumns = {
        @JoinColumn(name = "plan_startdate", referencedColumnName = "plan_startdate"),
        @JoinColumn(name = "department_id", referencedColumnName = "department_id")
    }, inverseJoinColumns = @JoinColumn(name = "shift_id"))
    private Set<Shift> shifts = new HashSet<>();

    public PlanBlueprint() {
    }

    public PlanBlueprint(LocalDate firstMondayInQuart, Department department) {
        this.id = new PlanId(firstMondayInQuart, department.getId());
        this.department = department;
    }

    public PlanId getId() {
        return id;
    }

    public void setId(PlanId id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        if (this.id == null) {
            this.id = new PlanId();
        }
        this.id.setDepartmentId(department.getId());
    }

    public Set<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(Set<Shift> shifts) {
        this.shifts = shifts;
    }

    public Month getMonth() {
        return this.id != null ? this.id.getStartDate().getMonth() : null;
    }
}
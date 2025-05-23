package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a blueprint for a plan, which includes the department and the shifts
 * associated with that plan.
 */
@Entity
@Table(name = "plan_blueprint")
public class PlanBlueprint {

    @EmbeddedId
    private PlanBlueprintId id;

    @MapsId("departmentId")
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany
    @JoinTable(name = "plan_shift", joinColumns = {
        @JoinColumn(name = "plan_startdate", referencedColumnName = "plan_startdate"),
        @JoinColumn(name = "department_id", referencedColumnName = "department_id")
    }, inverseJoinColumns = @JoinColumn(name = "shift_blueprint_id"))
    private Set<ShiftBlueprint> shiftBlueprints = new HashSet<>();

    public PlanBlueprint() {
    }

    public PlanBlueprint(LocalDate firstMondayInQuart, Department department) {
        this.id = new PlanBlueprintId(firstMondayInQuart, department.getId());
        this.department = department;
    }

    public PlanBlueprintId getId() {
        return id;
    }

    public void setId(PlanBlueprintId id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        if (this.id == null) {
            this.id = new PlanBlueprintId();
        }
        this.id.setDepartmentId(department.getId());
    }

    public Set<ShiftBlueprint> getShifts() {
        return shiftBlueprints;
    }

    public void setShifts(Set<ShiftBlueprint> shiftBlueprints) {
        this.shiftBlueprints = shiftBlueprints;
    }

    public Month getMonth() {
        return this.id != null ? this.id.getStartDate().getMonth() : null;
    }
}
package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * The PlanId class is used as a composite key for the Plan entity.
 * It consists of a start date and a department ID.
 */
@Embeddable
public class PlanBlueprintId implements Serializable {

    @Column(name = "plan_startdate", nullable = false)
    private LocalDate startDate;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    public PlanBlueprintId() {
    }

    public PlanBlueprintId(LocalDate startDate, Long departmentId) {
        this.startDate = startDate;
        this.departmentId = departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanBlueprintId that)) {
            return false;
        }
        return startDate == that.startDate && Objects.equals(departmentId, that.departmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, departmentId);
    }

    public LocalDate getStartDate() {
        return startDate;
    }
}
package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.converter.MonthConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Month;
import java.util.Objects;

@Embeddable
public class PlanId implements Serializable {

    @Convert(converter = MonthConverter.class)
    @Column(name = "plan_month", nullable = false)
    private Month month;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    public PlanId() {
    }

    public PlanId(Month month, Long departmentId) {
        this.month = month;
        this.departmentId = departmentId;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanId that)) {
            return false;
        }
        return month == that.month && Objects.equals(departmentId, that.departmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(month, departmentId);
    }
}

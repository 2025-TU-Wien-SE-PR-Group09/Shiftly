package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScheduledShiftId implements Serializable {

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "calendar_week")
    private int calendarWeek;

    @Column(name = "calendar_year")
    private int calendarYear;

    @Column(name = "shift_blueprint_id")
    private Long shiftBlueprintId;

    public ScheduledShiftId() {
    }

    public ScheduledShiftId(Long departmentId, int calendarWeek, int calendarYear, Long shiftBlueprintId) {
        this.departmentId = departmentId;
        this.calendarWeek = calendarWeek;
        this.calendarYear = calendarYear;
        this.shiftBlueprintId = shiftBlueprintId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public int getCalendarWeek() {
        return calendarWeek;
    }

    public void setCalendarWeek(int calendarWeek) {
        this.calendarWeek = calendarWeek;
    }

    public int getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(int calendarYear) {
        this.calendarYear = calendarYear;
    }

    public Long getShiftBlueprintId() {
        return shiftBlueprintId;
    }

    public void setShiftBlueprintId(Long shiftId) {
        this.shiftBlueprintId = shiftId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScheduledShiftId that)) {
            return false;
        }
        return calendarWeek == that.calendarWeek
            &&
            calendarYear == that.calendarYear
            &&
            Objects.equals(departmentId, that.departmentId)
            &&
            Objects.equals(shiftBlueprintId, that.shiftBlueprintId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId, calendarWeek, calendarYear, shiftBlueprintId);
    }
}
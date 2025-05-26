package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concrete_shift_plan")
public class ConcreteShiftPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(optional = false)
    private Department department;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduledShift> scheduledShifts = new ArrayList<>();

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<ScheduledShift> getScheduledShifts() {
        return scheduledShifts;
    }

    public void addScheduledShifts(List<ScheduledShift> scheduledShifts) {
        this.scheduledShifts = scheduledShifts;
        scheduledShifts.forEach(scheduledShift -> scheduledShift.setPlan(this));
    }

    public static class Builder {
        private final ConcreteShiftPlan plan = new ConcreteShiftPlan();

        public Builder withDepartment(Department department) {
            plan.setDepartment(department);
            return this;
        }

        public Builder withStartDate(LocalDate startDate) {
            plan.setStartDate(startDate);
            return this;
        }

        public Builder withEndDate(LocalDate endDate) {
            plan.setEndDate(endDate);
            return this;
        }

        public Builder addScheduledShift(ScheduledShift shift) {
            plan.getScheduledShifts().add(shift);
            shift.setPlan(plan);
            return this;
        }

        public ConcreteShiftPlan build() {
            return plan;
        }
    }

}

package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.config.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a blueprint for a plan, which includes the department and the shifts
 * associated with that plan.
 */
@Entity
@Table(name = "plan_blueprint")
public class PlanBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 255)
    private String description;

    public String getDescription() {
        return description;
    }

    @OneToMany(mappedBy = "planBlueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftBlueprint> shiftBlueprints = new ArrayList<>();

    public PlanBlueprint() {
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void addShiftBlueprint(ShiftBlueprint shiftBlueprint) {
        shiftBlueprints.add(shiftBlueprint);
        shiftBlueprint.setPlan(this);
    }

    public void removeShiftBlueprint(ShiftBlueprint shiftBlueprint) {
        shiftBlueprints.remove(shiftBlueprint);
        shiftBlueprint.setPlan(null);
    }

    public List<ShiftBlueprint> getShifts() {
        return shiftBlueprints;
    }

    public void setShifts(List<ShiftBlueprint> shiftBlueprints) {
        this.shiftBlueprints = shiftBlueprints;
    }

    public static class Builder {
        private final PlanBlueprint plan = new PlanBlueprint();

        public Builder withDepartment(Department department) {
            plan.setDepartment(department);
            return this;
        }

        public Builder withDescription(String description) {
            plan.setDescription(description);
            return this;
        }

        public Builder intercept(Consumer<PlanBlueprint> interceptor) {
            interceptor.accept(plan);
            return this;
        }

        public Builder addShift(Consumer<ShiftBlueprint.Builder> shiftConfig) {
            var shiftBuilder = new ShiftBlueprint.Builder();
            shiftConfig.accept(shiftBuilder);
            ShiftBlueprint shift = shiftBuilder.build();

            this.plan.addShiftBlueprint(shift);
            return this;
        }

        public Builder addShift(ShiftBlueprint shift) {
            this.plan.addShiftBlueprint(shift);
            shift.setPlan(plan);
            return this;
        }

        public Builder addShift(String description, int manpower, Consumer<ShiftBlueprint> config) {
            ShiftBlueprint shift = new ShiftBlueprint();
            shift.setDescription(description);
            shift.setManPower(manpower);
            shift.setPlan(plan);
            config.accept(shift);
            plan.getShifts().add(shift);
            return this;
        }

        public PlanBlueprint build() {
            return plan;
        }
    }
}
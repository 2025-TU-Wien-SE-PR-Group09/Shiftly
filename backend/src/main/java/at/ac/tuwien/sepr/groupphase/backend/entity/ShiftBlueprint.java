package at.ac.tuwien.sepr.groupphase.backend.entity;

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
 * Represents a shift in the system.
 * A shift has a description and a required manPower.
 * It can be associated with multiple plans and can have multiple shiftWeeks.
 */
@Entity
@Table(name = "shift_blueprint")
public class ShiftBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String description;

    @Column(nullable = false)
    private int manPower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_blueprint_id")
    private PlanBlueprint planBlueprint;

    @OneToMany(mappedBy = "shiftBlueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftWeekBlueprint> weeks = new ArrayList<>();

    public ShiftBlueprint() {
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public PlanBlueprint getPlan() {
        return planBlueprint;
    }

    public int getManPower() {
        return this.manPower;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPlan(PlanBlueprint plan) {
        this.planBlueprint = plan;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void addWeeks(List<ShiftWeekBlueprint> weeks) {
        this.weeks.addAll(weeks);
        for (ShiftWeekBlueprint week : weeks) {
            week.setShiftBlueprint(this);
        }
    }

    public void setWeeks(List<ShiftWeekBlueprint> weeks) {
        this.weeks = weeks;
    }

    public void setManPower(int manPower) {
        this.manPower = manPower;
    }

    public List<ShiftWeekBlueprint> getShiftWeeks() {
        return weeks;
    }

    public static class Builder {
        private final ShiftBlueprint shift = new ShiftBlueprint();

        public Builder withDescription(String description) {
            shift.setDescription(description);
            return this;
        }

        public Builder withManPower(int manPower) {
            shift.setManPower(manPower);
            return this;
        }

        public Builder intercept(Consumer<ShiftBlueprint> interceptor) {
            interceptor.accept(shift);
            return this;
        }

        public ShiftBlueprint.Builder addWeek(int index, Consumer<ShiftWeekBlueprint.Builder> weekConfig) {
            var weekBuilder = new ShiftWeekBlueprint.Builder()
                .withIndex(index);
            weekConfig.accept(weekBuilder);
            ShiftWeekBlueprint week = weekBuilder.build();

            this.shift.addWeeks(List.of(week));
            return this;
        }

        public ShiftBlueprint.Builder addWeek(ShiftWeekBlueprint week) {
            this.shift.addWeeks(List.of(week));
            return this;
        }

        public ShiftBlueprint build() {
            return shift;
        }
    }
}
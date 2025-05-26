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
 * Represents a week of shifts in the system.
 * A shift week is associated with a specific shift and contains multiple shiftDays.
 */
@Entity
@Table(name = "shift_week_blueprint")
public class ShiftWeekBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer weekIndex;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_blueprint_id")
    private ShiftBlueprint shiftBlueprint;

    @OneToMany(mappedBy = "shiftWeekBlueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftDayBlueprint> days = new ArrayList<ShiftDayBlueprint>();

    public ShiftWeekBlueprint() {
    }

    public List<ShiftDayBlueprint> getDays() {
        return days;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setWeekIndex(Integer weekIndex) {
        this.weekIndex = weekIndex;
    }

    public Integer getWeekIndex() {
        return weekIndex;
    }

    public void addShiftDays(List<ShiftDayBlueprint> shiftDays) {
        this.days.addAll(shiftDays);
        for (ShiftDayBlueprint day : shiftDays) {
            day.setShiftWeekBlueprint(this);
        }
    }

    public void setShiftBlueprint(ShiftBlueprint shiftBlueprint) {
        this.shiftBlueprint = shiftBlueprint;
    }

    public ShiftBlueprint getShiftBlueprint() {
        return shiftBlueprint;
    }

    public void setDays(List<ShiftDayBlueprint> shiftDays) {
        this.days = shiftDays;
    }

    public static class Builder {
        private final ShiftWeekBlueprint week = new ShiftWeekBlueprint();

        public Builder withIndex(int index) {
            week.setWeekIndex(index);
            return this;
        }

        public Builder withDay(Consumer<ShiftDayBlueprint.Builder> config) {
            var dayBuilder = new ShiftDayBlueprint.Builder();
            config.accept(dayBuilder);
            var day = dayBuilder.build();

            week.addShiftDays(List.of(day));
            return this;
        }

        public Builder withDays(List<ShiftDayBlueprint> days) {
            this.week.addShiftDays(days);
            return this;
        }

        public ShiftWeekBlueprint build() {
            return week;
        }
    }
}
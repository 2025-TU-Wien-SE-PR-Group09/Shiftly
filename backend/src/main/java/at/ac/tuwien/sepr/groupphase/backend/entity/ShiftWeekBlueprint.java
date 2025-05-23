package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * Represents a week of shifts in the system.
 * A shift week is associated with a specific shift and contains multiple days.
 */
@Entity
@Table(name = "shift_week_blueprint")
public class ShiftWeekBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer weekIndex;

    @ManyToOne
    @JoinColumn(name = "shift_blueprint_id")
    private ShiftBlueprint shiftBlueprint;

    @OneToMany(mappedBy = "shiftWeekBlueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftDayBlueprint> days = new ArrayList<ShiftDayBlueprint>();

    public ShiftWeekBlueprint() {
    }

    public ShiftWeekBlueprint(Integer weekIndex, ShiftBlueprint shiftBlueprint) {
        this.weekIndex = weekIndex;
        this.shiftBlueprint = shiftBlueprint;
    }

    public List<ShiftDayBlueprint> getDays() {
        return days;
    }

    public void setDays(List<ShiftDayBlueprint> shiftdays) {
        this.days = shiftdays;
    }
}
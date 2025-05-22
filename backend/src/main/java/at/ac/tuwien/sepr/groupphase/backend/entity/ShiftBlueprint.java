package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a shift in the system.
 * A shift has a description and a required manpower.
 * It can be associated with multiple plans and can have multiple weeks.
 */
@Entity
@Table(name = "shift_blueprint")
public class ShiftBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String description;

    @Column(nullable = false)
    private int manPower;

    @ManyToMany(mappedBy = "shiftBlueprints")
    private Set<PlanBlueprint> plans;

    @OneToMany(mappedBy = "shiftBlueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftWeekBlueprint> weeks = new ArrayList<ShiftWeekBlueprint>();

    public ShiftBlueprint() {
    }

    public ShiftBlueprint(String description, int manPower) {
        this.description = description;
        this.manPower = manPower;
    }

    public ShiftBlueprint(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<PlanBlueprint> getPlans() {
        return plans;
    }

    public void setPlans(Set<PlanBlueprint> plans) {
        this.plans = plans;
    }

    public int getManPower() {
        return this.manPower;
    }

    public List<ShiftWeekBlueprint> getShiftWeeks() {
        return weeks;
    }

    public void setId(long id) {
        this.id = id;
    }
}
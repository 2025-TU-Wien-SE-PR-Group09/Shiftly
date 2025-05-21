package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a shift in the system.
 * A shift has a description and a required manpower.
 * It can be associated with multiple plans and can have multiple weeks.
 */
@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String description;

    @Column(nullable = false)
    private int manPower;

    @ManyToMany(mappedBy = "shifts")
    private Set<PlanBlueprint> plans;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftWeek> weeks = new ArrayList<ShiftWeek>();

    public Shift() {
    }

    public Shift(String description, int manPower) {
        this.description = description;
        this.manPower = manPower;
    }

    public Shift(String description) {
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

    public List<ShiftWeek> getShiftWeeks() {
        return weeks;
    }

    public void setId(long id) {
        this.id = id;
    }
}
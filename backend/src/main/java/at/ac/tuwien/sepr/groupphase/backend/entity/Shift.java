package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToMany(mappedBy = "shifts")
    private Set<Plan> plans;

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftWeek> weeks = new ArrayList<ShiftWeek>();

    public Shift() {
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

    public Set<Plan> getPlans() {
        return plans;
    }

    public void setPlans(Set<Plan> plans) {
        this.plans = plans;
    }
}

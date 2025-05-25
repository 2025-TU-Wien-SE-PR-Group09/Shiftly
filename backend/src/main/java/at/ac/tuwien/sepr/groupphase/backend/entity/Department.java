package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "department")
    private Set<ApplicationUser> users = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConcreteShiftPlan> shiftPlans;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PlanBlueprint> plans = new HashSet<>();

    public Set<PlanBlueprint> getPlans() {
        return plans;
    }

    public void setPlans(Set<PlanBlueprint> plans) {
        this.plans = plans;
    }

    public Set<ApplicationUser> getUsers() {
        return users;
    }

    public void addPlan(PlanBlueprint plan) {
        plans.add(plan);
        plan.setDepartment(this);
    }

    public void setUsers(Set<ApplicationUser> users) {
        this.users = users;
    }

    public List<ConcreteShiftPlan> getShiftPlans() {
        return shiftPlans;
    }

    public void setShiftPlans(List<ConcreteShiftPlan> shiftPlans) {
        this.shiftPlans = shiftPlans;
    }
}
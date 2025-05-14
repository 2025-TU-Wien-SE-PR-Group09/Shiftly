package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_email")
    private ApplicationUser supervisor;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ApplicationUser getSupervisor() {
        return supervisor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSupervisor(ApplicationUser supervisor) {
        this.supervisor = supervisor;
    }
}

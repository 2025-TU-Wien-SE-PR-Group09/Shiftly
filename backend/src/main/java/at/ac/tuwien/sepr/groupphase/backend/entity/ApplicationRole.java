package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class ApplicationRole {
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<ApplicationUser> users = new HashSet<>();

    @Id
    @Column(unique = true, nullable = false)
    private String name;

    public ApplicationRole(String name) {
        this.name = name;
    }

    public ApplicationRole(Role role, Set<ApplicationUser> users) {
        this.name = role.name();
        this.users = users;
    }

    public ApplicationRole(String name, Set<ApplicationUser> users) {
        this.name = name;
        this.users = users;
    }

    public ApplicationRole() {
    }

    public String getName() {
        return name;
    }

    public Set<ApplicationUser> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "ApplicationRole{"
            + "users=" + users
            + ", name='" + name + '\''
            + '}';
    }
}

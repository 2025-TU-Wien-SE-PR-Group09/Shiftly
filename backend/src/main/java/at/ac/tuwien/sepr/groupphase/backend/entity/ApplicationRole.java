package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class ApplicationRole {
    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private Set<ApplicationUser> users  = new HashSet<>();

    @Id
    @Column(unique = true, nullable = false)
    private String name;

    public ApplicationRole(String name) {
        this.name = name;
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

    public void setName(String name) {
        this.name = name;
    }

    public Set<ApplicationUser> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "ApplicationRole{" +
            "users=" + users +
            ", name='" + name + '\'' +
            '}';
    }
}

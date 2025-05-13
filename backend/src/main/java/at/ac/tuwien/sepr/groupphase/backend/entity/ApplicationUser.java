package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
public class ApplicationUser {
    @Id
    @Column(unique = true, nullable = false, length = 50)
    @Size(max = 50)
    private String email;

    @Column(nullable = false, length = 200)
    @Size(max = 200)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "application_user_role_binding",
        joinColumns = @JoinColumn(name = "user_email"),
        inverseJoinColumns = @JoinColumn(name = "role_role")
    )
    private Set<ApplicationRole> roles = new HashSet<>();

    public ApplicationUser() {
    }

    public ApplicationUser(String email, String password) {
        this.email = email;
        this.passwordHash = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String password) {
        this.passwordHash = password;
    }

    public Set<ApplicationRole> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "ApplicationUser{"
            + "email='" + email + '\''
            + ", password='" + passwordHash + '\''
            + ", roles=" + roles
            + '}';
    }
}

package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class ApplicationUser {
    @Id
    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "application_user_role_binging",
        joinColumns = @JoinColumn(name = "user_email"),
        inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<ApplicationRole> roles = new HashSet<>();

    public ApplicationUser() {
    }

    public ApplicationUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<ApplicationRole> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "ApplicationUser{"
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + ", roles=" + roles
            + '}';
    }
}

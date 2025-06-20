package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
public class ApplicationUser {
    @Id
    @Column(unique = true, nullable = false, length = 200)
    @Size(max = 200)
    private String email;

    @Column(nullable = false, length = 50)
    @Size(max = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    @Size(max = 50)
    private String lastName;

    @Column(nullable = false, length = 200)
    @Size(max = 200)
    private String passwordHash;

    @JoinColumn()
    @ManyToOne(fetch = FetchType.EAGER)
    private Department department;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ScheduledShiftAssignment> assignments = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "application_user_role_binding",
        joinColumns = @JoinColumn(name = "user_email"),
        inverseJoinColumns = @JoinColumn(name = "role_role")
    )
    private final Set<ApplicationRole> roles = new HashSet<>();

    public ApplicationUser() {
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
            + ", roles=" + roles.stream().map(ApplicationRole::getName).toList()
            + ", firstName='" + firstName + '\''
            + ", lastName='" + lastName + '\''
            + '}';
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<ScheduledShiftAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ScheduledShiftAssignment> assignments) {
        this.assignments = assignments;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationUser that = (ApplicationUser) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

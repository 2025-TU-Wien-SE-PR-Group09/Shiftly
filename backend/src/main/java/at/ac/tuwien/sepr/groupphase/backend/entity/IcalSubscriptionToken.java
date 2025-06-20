package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing an iCal subscription token for a user.
 * This allows calendar applications to subscribe to a user's shifts
 * and receive updates when the subscription URL is accessed.
 */
@Entity
@Table(name = "ical_subscription_token", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_email"})
})
public class IcalSubscriptionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "user_email", nullable = false, length = 200)
    private String userEmail;

    @NotNull
    @Size(max = 255)
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    public IcalSubscriptionToken() {
    }

    public IcalSubscriptionToken(String userEmail, String token) {
        this.userEmail = userEmail;
        this.token = token;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IcalSubscriptionToken that = (IcalSubscriptionToken) o;
        return Objects.equals(id, that.id) && Objects.equals(userEmail, that.userEmail) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userEmail, token);
    }

    @Override
    public String toString() {
        return "IcalSubscriptionToken{"
            + "id=" + id
            + ", userEmail='" + userEmail + '\''
            + ", token='" + token + '\''
            + ", createdAt=" + createdAt
            + ", lastAccessedAt=" + lastAccessedAt
            + '}';
    }
}
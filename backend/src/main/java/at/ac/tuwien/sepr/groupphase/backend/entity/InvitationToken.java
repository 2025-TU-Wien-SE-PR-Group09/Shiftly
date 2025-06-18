package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class InvitationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotNull
    @Size(max = 1000)
    private String token;

    @Column(nullable = false)
    @NotNull
    @Email
    @Size(max = 200)
    private String email;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    public InvitationToken() {
    }

    public InvitationToken(String token, String email, LocalDateTime expiryDate) {
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvitationToken that = (InvitationToken) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token);
    }
}
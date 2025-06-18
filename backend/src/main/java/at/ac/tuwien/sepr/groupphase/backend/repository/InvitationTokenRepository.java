package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {
    Optional<InvitationToken> findByToken(String token);
    Optional<InvitationToken> findByEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.IcalSubscriptionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IcalSubscriptionTokenRepository extends JpaRepository<IcalSubscriptionToken, Long> {

    /**
     * Find a subscription token by the user's email address.
     *
     * @param userEmail the email address of the user
     * @return an Optional containing the token if found
     */
    Optional<IcalSubscriptionToken> findByUserEmail(String userEmail);

    /**
     * Find a subscription token by the token value.
     *
     * @param token the token value to search for
     * @return an Optional containing the token if found
     */
    Optional<IcalSubscriptionToken> findByToken(String token);

    /**
     * Delete all tokens that are older than the specified date.
     * This can be used for cleanup of expired tokens.
     *
     * @param olderThan the date threshold for deletion
     * @return the number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM IcalSubscriptionToken t WHERE t.createdAt < :olderThan")
    int deleteTokensOlderThan(@Param("olderThan") LocalDateTime olderThan);

    /**
     * Update the last accessed timestamp for a token.
     *
     * @param tokenId the ID of the token to update
     * @param lastAccessedAt the new last accessed timestamp
     */
    @Modifying
    @Query("UPDATE IcalSubscriptionToken t SET t.lastAccessedAt = :lastAccessedAt WHERE t.id = :tokenId")
    void updateLastAccessedAt(@Param("tokenId") Long tokenId, @Param("lastAccessedAt") LocalDateTime lastAccessedAt);
} 
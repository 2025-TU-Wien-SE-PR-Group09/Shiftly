package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.IcalSubscriptionToken;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing iCal subscription tokens.
 * Handles creation, retrieval, and validation of subscription tokens.
 */
public interface IcalSubscriptionTokenService {

    /**
     * Generate or retrieve an existing subscription token for a user.
     * If a token already exists for the user, it will be returned.
     * If no token exists, a new one will be created.
     *
     * @param userEmail the email address of the user
     * @return the subscription token for the user
     */
    IcalSubscriptionToken getOrCreateToken(String userEmail);

    /**
     * Find a subscription token by its token value.
     *
     * @param token the token value to search for
     * @return an Optional containing the token if found
     */
    Optional<IcalSubscriptionToken> findByToken(String token);

    /**
     * Validate a subscription token and update its last accessed timestamp.
     * This method should be called when a token is used to access the iCal feed.
     *
     * @param token the token value to validate
     * @return an Optional containing the validated token if valid
     */
    Optional<IcalSubscriptionToken> validateAndUpdateToken(String token);

    /**
     * Delete a subscription token for a user.
     * This can be used to revoke access to the iCal feed.
     *
     * @param userEmail the email address of the user
     * @return true if the token was deleted, false if no token existed
     */
    
    boolean deleteToken(String userEmail);

    /**
     * Clean up old tokens that are no longer needed.
     * This method can be called periodically to remove expired tokens.
     *
     * @param olderThanDays the age threshold in days for token cleanup
     * @return the number of tokens that were deleted
     */
    int cleanupOldTokens(int olderThanDays);

    /**
     * Get all tokens in the database (for debugging purposes).
     *
     * @return a list of all tokens
     */
    List<IcalSubscriptionToken> getAllTokens();
}
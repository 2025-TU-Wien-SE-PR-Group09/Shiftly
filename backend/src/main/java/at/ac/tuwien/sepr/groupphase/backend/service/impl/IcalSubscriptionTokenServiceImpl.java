package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.IcalSubscriptionToken;
import at.ac.tuwien.sepr.groupphase.backend.repository.IcalSubscriptionTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.IcalSubscriptionTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IcalSubscriptionTokenServiceImpl implements IcalSubscriptionTokenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IcalSubscriptionTokenRepository tokenRepository;

    public IcalSubscriptionTokenServiceImpl(IcalSubscriptionTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public IcalSubscriptionToken getOrCreateToken(String userEmail) {
        LOGGER.trace("getOrCreateToken({})", userEmail);
        
        // Try to find existing token
        Optional<IcalSubscriptionToken> existingToken = tokenRepository.findByUserEmail(userEmail);
        
        if (existingToken.isPresent()) {
            IcalSubscriptionToken token = existingToken.get();
            LOGGER.info("Found existing token for user {}: {}", userEmail, token.getToken());
            return token;
        }
        
        // Create new token
        String tokenValue = UUID.randomUUID().toString();
        LOGGER.info("Creating new token for user {}: {}", userEmail, tokenValue);
        
        IcalSubscriptionToken newToken = new IcalSubscriptionToken(userEmail, tokenValue);
        IcalSubscriptionToken savedToken = tokenRepository.save(newToken);
        
        LOGGER.info("Successfully created new subscription token for user {}: {}", userEmail, savedToken.getToken());
        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IcalSubscriptionToken> findByToken(String token) {
        LOGGER.trace("findByToken({})", token);
        Optional<IcalSubscriptionToken> result = tokenRepository.findByToken(token);
        if (result.isPresent()) {
            LOGGER.debug("Found token in database for token: {}", token);
        } else {
            LOGGER.debug("No token found in database for token: {}", token);
        }
        return result;
    }

    @Override
    @Transactional
    public Optional<IcalSubscriptionToken> validateAndUpdateToken(String token) {
        LOGGER.trace("validateAndUpdateToken({})", token);
        
        Optional<IcalSubscriptionToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isPresent()) {
            IcalSubscriptionToken subscriptionToken = tokenOpt.get();
            LOGGER.debug("Found valid token for user: {}", subscriptionToken.getUserEmail());
            
            // Update the last accessed timestamp
            LocalDateTime now = LocalDateTime.now();
            subscriptionToken.setLastAccessedAt(now);
            
            // Save the updated token
            IcalSubscriptionToken updatedToken = tokenRepository.save(subscriptionToken);
            LOGGER.debug("Successfully updated last accessed timestamp for token of user {} to {}", 
                updatedToken.getUserEmail(), updatedToken.getLastAccessedAt());
            return Optional.of(updatedToken);
        }
        
        LOGGER.warn("Invalid subscription token: {}", token);
        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean deleteToken(String userEmail) {
        LOGGER.trace("deleteToken({})", userEmail);
        
        Optional<IcalSubscriptionToken> tokenOpt = tokenRepository.findByUserEmail(userEmail);
        
        if (tokenOpt.isPresent()) {
            tokenRepository.delete(tokenOpt.get());
            LOGGER.info("Deleted subscription token for user {}", userEmail);
            return true;
        }
        
        LOGGER.debug("No subscription token found for user {}", userEmail);
        return false;
    }

    @Override
    @Transactional
    public int cleanupOldTokens(int olderThanDays) {
        LOGGER.trace("cleanupOldTokens({} days)", olderThanDays);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
        int deletedCount = tokenRepository.deleteTokensOlderThan(cutoffDate);
        
        LOGGER.info("Cleaned up {} old subscription tokens", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IcalSubscriptionToken> getAllTokens() {
        LOGGER.trace("getAllTokens()");
        List<IcalSubscriptionToken> tokens = tokenRepository.findAll();
        LOGGER.debug("Found {} tokens in database", tokens.size());
        return tokens;
    }

    /**
     * Scheduled task to clean up old tokens that haven't been accessed in 90 days.
     * Runs every day at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldTokensScheduled() {
        LOGGER.info("Starting scheduled cleanup of old subscription tokens");
        int deletedCount = cleanupOldTokens(90);
        LOGGER.info("Scheduled cleanup completed. Deleted {} old tokens", deletedCount);
    }
} 
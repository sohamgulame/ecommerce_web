package com.Project1.project.service.impl;

import com.Project1.project.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Periodically removes revoked or expired refresh tokens.
     * Runs daily at 2:00 AM server time.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = refreshTokenRepository.deleteExpiredOrRevokedTokens(Instant.now());
            if (deletedCount > 0) {
                log.info("Cleaned up {} expired/revoked refresh tokens from database", deletedCount);
            }
        } catch (Exception e) {
            log.error("Failed to clean up expired refresh tokens", e);
        }
    }
}

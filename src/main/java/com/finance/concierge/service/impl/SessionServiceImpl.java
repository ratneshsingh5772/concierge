package com.finance.concierge.service.impl;

import com.finance.concierge.dto.SessionResetRequestDTO;
import com.finance.concierge.entity.UserSession;
import com.finance.concierge.exception.SessionException;
import com.finance.concierge.repository.UserSessionRepository;
import com.finance.concierge.service.SessionService;
import com.finance.concierge.util.SessionUtils;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of SessionService with MySQL persistence
 * Following Single Responsibility Principle (SRP)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final InMemoryRunner runner;
    private final UserSessionRepository userSessionRepository;
    private final Map<String, Session> sessionCache = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public String resetSession(SessionResetRequestDTO request) {
        String userId = request.getUserIdOrDefault();
        log.info("Resetting session for user: {}", userId);

        try {
            // Remove from cache
            Session removedSession = sessionCache.remove(userId);

            // Deactivate in database
            userSessionRepository.deactivateSession(userId);

            if (removedSession != null) {
                log.info("Session reset successfully for user: {}", userId);
                return String.format("Session reset successfully for user: %s", userId);
            } else {
                log.warn("No active session found for user: {}", userId);
                return String.format("No active session found for user: %s", userId);
            }

        } catch (Exception e) {
            log.error("Error resetting session for user: {}", userId, e);
            throw new SessionException("Failed to reset session for user: " + userId, e);
        }
    }

    @Override
    @Transactional
    public Session getOrCreateSession(String userId) {
        String normalizedUserId = SessionUtils.getUserIdOrDefault(userId);
        log.debug("Getting or creating session for user: {}", normalizedUserId);

        try {
            // Check cache first
            Session cachedSession = sessionCache.get(normalizedUserId);
            if (cachedSession != null) {
                log.debug("Returning cached session for user: {}", normalizedUserId);
                updateLastActivity(normalizedUserId);
                return cachedSession;
            }

            // Check database for existing active session
            Optional<UserSession> existingSession = userSessionRepository
                    .findByUserIdAndIsActiveTrue(normalizedUserId);

            if (existingSession.isPresent()) {
                log.info("Restoring existing session from database for user: {}", normalizedUserId);
                Session restoredSession = createSessionFromEntity(existingSession.get());
                sessionCache.put(normalizedUserId, restoredSession);
                updateLastActivity(normalizedUserId);
                return restoredSession;
            }

            // Create new session
            log.info("Creating new session for user: {}", normalizedUserId);
            Session newSession = runner.sessionService()
                    .createSession(runner.appName(), normalizedUserId)
                    .blockingGet();

            if (!SessionUtils.isValidSession(newSession)) {
                throw new SessionException("Failed to create valid session for user: " + normalizedUserId);
            }

            // Save to database
            UserSession userSession = UserSession.builder()
                    .userId(normalizedUserId)
                    .sessionId(newSession.id())
                    .appName(runner.appName())
                    .isActive(true)
                    .lastActivity(LocalDateTime.now())
                    .build();

            userSessionRepository.save(userSession);

            // Cache the session
            sessionCache.put(normalizedUserId, newSession);

            log.info("Session created and saved for user: {}", normalizedUserId);
            return newSession;

        } catch (Exception e) {
            log.error("Error getting or creating session for user: {}", normalizedUserId, e);
            throw new SessionException("Failed to get or create session for user: " + normalizedUserId, e);
        }
    }

    @Override
    public boolean hasSession(String userId) {
        String normalizedUserId = SessionUtils.getUserIdOrDefault(userId);

        // Check cache first
        if (sessionCache.containsKey(normalizedUserId)) {
            return true;
        }

        // Check database
        boolean exists = userSessionRepository.existsByUserIdAndIsActiveTrue(normalizedUserId);
        log.debug("Session exists for user {}: {}", normalizedUserId, exists);
        return exists;
    }

    /**
     * Create Session object from database entity
     */
    private Session createSessionFromEntity(UserSession userSession) {
        try {
            // Recreate session using the stored session ID
            return runner.sessionService()
                    .createSession(userSession.getAppName(), userSession.getUserId())
                    .blockingGet();
        } catch (Exception e) {
            log.error("Error recreating session from entity", e);
            throw new SessionException("Failed to recreate session", e);
        }
    }

    /**
     * Update last activity timestamp
     */
    @Transactional
    protected void updateLastActivity(String userId) {
        try {
            userSessionRepository.updateLastActivity(userId, LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Failed to update last activity for user: {}", userId, e);
        }
    }
}


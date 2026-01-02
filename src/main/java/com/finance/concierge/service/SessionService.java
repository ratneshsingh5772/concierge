package com.finance.concierge.service;

import com.finance.concierge.dto.SessionResetRequestDTO;

/**
 * Service interface for session management operations
 * Following Interface Segregation Principle (ISP) from SOLID
 */
public interface SessionService {

    /**
     * Reset session for a user
     *
     * @param request the session reset request DTO
     * @return success message
     */
    String resetSession(SessionResetRequestDTO request);

    /**
     * Get or create session for a user
     *
     * @param userId the user identifier
     * @return session object
     */
    com.google.adk.sessions.Session getOrCreateSession(String userId);

    /**
     * Check if session exists for a user
     *
     * @param userId the user identifier
     * @return true if session exists
     */
    boolean hasSession(String userId);
}


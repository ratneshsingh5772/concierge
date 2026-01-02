package com.finance.concierge.util;

import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import lombok.experimental.UtilityClass;

/**
 * Utility class for session management operations
 */
@UtilityClass
public class SessionUtils {

    private static final String DEFAULT_USER_ID = "default-user";

    /**
     * Get user ID with default fallback
     */
    public static String getUserIdOrDefault(String userId) {
        return userId != null && !userId.isBlank() ? userId : DEFAULT_USER_ID;
    }

    /**
     * Get default user ID constant
     */
    public static String getDefaultUserId() {
        return DEFAULT_USER_ID;
    }

    /**
     * Create session identifier
     */
    public static String createSessionIdentifier(String userId, String sessionId) {
        return String.format("%s:%s", userId, sessionId);
    }

    /**
     * Validate session
     */
    public static boolean isValidSession(Session session) {
        return session != null && session.id() != null && session.userId() != null;
    }
}


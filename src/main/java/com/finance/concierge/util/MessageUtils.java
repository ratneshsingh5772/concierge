package com.finance.concierge.util;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import lombok.experimental.UtilityClass;

/**
 * Utility class for message processing operations
 */
@UtilityClass
public class MessageUtils {

    /**
     * Create Content from text message
     */
    public static Content createContentFromText(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or blank");
        }
        return Content.fromParts(Part.fromText(message));
    }

    /**
     * Validate message content
     */
    public static boolean isValidMessage(String message) {
        return message != null && !message.isBlank();
    }

    /**
     * Sanitize message (basic sanitization)
     */
    public static String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.trim();
    }
}


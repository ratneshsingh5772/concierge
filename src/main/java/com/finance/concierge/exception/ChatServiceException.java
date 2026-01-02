package com.finance.concierge.exception;

/**
 * Custom exception for chat service related errors
 */
public class ChatServiceException extends RuntimeException {

    public ChatServiceException(String message) {
        super(message);
    }

    public ChatServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}


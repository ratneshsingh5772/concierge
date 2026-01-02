package com.finance.concierge.service;

import com.finance.concierge.dto.AuthResponseDTO;
import com.finance.concierge.dto.LoginRequestDTO;
import com.finance.concierge.dto.RegisterRequestDTO;
import com.finance.concierge.entity.User;

/**
 * Service interface for authentication operations
 */
public interface AuthService {

    /**
     * Register new user
     */
    AuthResponseDTO register(RegisterRequestDTO request);

    /**
     * Authenticate user and generate tokens
     */
    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Refresh access token
     */
    AuthResponseDTO refreshToken(String refreshToken);

    /**
     * Get current authenticated user
     */
    User getCurrentUser();

    /**
     * Logout user (invalidate tokens if needed)
     */
    void logout(String token);
}


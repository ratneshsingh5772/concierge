package com.finance.concierge.service.impl;

import com.finance.concierge.dto.AuthResponseDTO;
import com.finance.concierge.dto.LoginRequestDTO;
import com.finance.concierge.dto.RegisterRequestDTO;
import com.finance.concierge.entity.User;
import com.finance.concierge.exception.AuthenticationException;
import com.finance.concierge.repository.UserRepository;
import com.finance.concierge.service.AuthService;
import com.finance.concierge.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of AuthService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Generate username from email
        String username = generateUsernameFromEmail(request.getEmail());
        log.info("Username generated: {}", username);

        log.info("Registering new user: {}", username);

        // Check if username exists
        if (userRepository.existsByUsername(username)) {
            throw new AuthenticationException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // Generate tokens
        String accessToken = jwtUtils.generateToken(savedUser);
        String refreshToken = jwtUtils.generateRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * Generate a username from email address
     */
    private String generateUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new AuthenticationException("Invalid email for username generation");
        }

        String baseUsername = email.split("@")[0];

        // Ensure username meets length requirements (max 50)
        if (baseUsername.length() > 45) {
            baseUsername = baseUsername.substring(0, 45);
        }

        // Check if base username exists, if so append random number
        String username = baseUsername;
        int attempt = 0;
        while (userRepository.existsByUsername(username)) {
            attempt++;
            // Append random 4 digit number
            int randomNum = 1000 + (int)(Math.random() * 9000);
            username = baseUsername + randomNum;

            // Safety break
            if (attempt > 10) {
                throw new AuthenticationException("Unable to generate unique username");
            }
        }

        return username;
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("User login attempt: {}", request.getUsernameOrEmail());

        // Authenticate user
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsernameOrEmail());
            throw new AuthenticationException("Invalid username or password");
        }

        // Find user
        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail()
                )
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Update last login
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        log.info("User logged in successfully: {}", user.getUsername());

        // Generate tokens
        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        // Validate refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        // Extract username from token
        String username = jwtUtils.extractUsername(refreshToken);

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Generate new tokens
        String newAccessToken = jwtUtils.generateToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);

        log.info("Tokens refreshed for user: {}", username);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public User getCurrentUser() {
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    @Override
    public void logout(String token) {
        log.info("User logout");
        // Token invalidation can be implemented with Redis/blacklist if needed
        SecurityContextHolder.clearContext();
    }

    /**
     * Build authentication response DTO
     */
    private AuthResponseDTO buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponseDTO.UserInfoDTO userInfo = AuthResponseDTO.UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationTime())
                .user(userInfo)
                .build();
    }
}


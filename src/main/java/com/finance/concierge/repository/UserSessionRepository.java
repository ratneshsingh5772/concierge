package com.finance.concierge.repository;

import com.finance.concierge.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for UserSession entity
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Find active session by user ID
     */
    Optional<UserSession> findByUserIdAndIsActiveTrue(String userId);

    /**
     * Find session by user ID (regardless of active status)
     */
    Optional<UserSession> findByUserId(String userId);

    /**
     * Deactivate session for user
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    void deactivateSession(@Param("userId") String userId);

    /**
     * Update last activity timestamp
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.lastActivity = :timestamp WHERE s.userId = :userId")
    void updateLastActivity(@Param("userId") String userId, @Param("timestamp") LocalDateTime timestamp);

    /**
     * Check if user has active session
     */
    boolean existsByUserIdAndIsActiveTrue(String userId);
}


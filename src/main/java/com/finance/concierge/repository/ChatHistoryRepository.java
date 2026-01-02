package com.finance.concierge.repository;

import com.finance.concierge.entity.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ChatHistory entity
 */
@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    /**
     * Find all chat history for a user, ordered by creation time
     */
    List<ChatHistory> findByUserIdOrderByCreatedAtAsc(String userId);

    /**
     * Find chat history for a user with pagination
     */
    Page<ChatHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find recent chat history for a user (last N messages)
     */
    @Query("SELECT c FROM ChatHistory c WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<ChatHistory> findRecentHistory(@Param("userId") String userId, Pageable pageable);

    /**
     * Find chat history by session ID
     */
    List<ChatHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * Find chat history within a date range
     */
    @Query("SELECT c FROM ChatHistory c WHERE c.userId = :userId AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt ASC")
    List<ChatHistory> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count total messages for a user
     */
    long countByUserId(String userId);

    /**
     * Delete old chat history (older than specified date)
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
}


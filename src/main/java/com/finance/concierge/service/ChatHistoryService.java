package com.finance.concierge.service;

import com.finance.concierge.entity.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for chat history management
 */
public interface ChatHistoryService {

    /**
     * Save chat interaction to history
     */
    ChatHistory saveChatHistory(String userId, String sessionId, String userMessage, String agentResponse);

    /**
     * Get all chat history for a user
     */
    List<ChatHistory> getUserChatHistory(String userId);

    /**
     * Get paginated chat history for a user
     */
    Page<ChatHistory> getUserChatHistoryPaginated(String userId, Pageable pageable);

    /**
     * Get recent chat history (last N messages)
     */
    List<ChatHistory> getRecentHistory(String userId, int limit);

    /**
     * Get chat history for a specific session
     */
    List<ChatHistory> getSessionHistory(String sessionId);

    /**
     * Get chat history within date range
     */
    List<ChatHistory> getHistoryByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get formatted conversation context for AI
     */
    String getConversationContext(String userId, int maxMessages);

    /**
     * Count total messages for a user
     */
    long countUserMessages(String userId);
}


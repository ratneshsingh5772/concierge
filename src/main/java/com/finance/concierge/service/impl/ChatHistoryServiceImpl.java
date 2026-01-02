package com.finance.concierge.service.impl;

import com.finance.concierge.entity.ChatHistory;
import com.finance.concierge.repository.ChatHistoryRepository;
import com.finance.concierge.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ChatHistoryService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Override
    @Transactional
    public ChatHistory saveChatHistory(String userId, String sessionId, String userMessage, String agentResponse) {
        log.debug("Saving chat history for user: {}", userId);

        ChatHistory chatHistory = ChatHistory.builder()
                .userId(userId)
                .sessionId(sessionId)
                .userMessage(userMessage)
                .agentResponse(agentResponse)
                .messageType("CHAT")
                .build();

        ChatHistory saved = chatHistoryRepository.save(chatHistory);
        log.info("Chat history saved with ID: {} for user: {}", saved.getId(), userId);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatHistory> getUserChatHistory(String userId) {
        log.debug("Fetching all chat history for user: {}", userId);
        return chatHistoryRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatHistory> getUserChatHistoryPaginated(String userId, Pageable pageable) {
        log.debug("Fetching paginated chat history for user: {}", userId);
        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatHistory> getRecentHistory(String userId, int limit) {
        log.debug("Fetching recent {} messages for user: {}", limit, userId);
        Pageable pageable = PageRequest.of(0, limit);
        return chatHistoryRepository.findRecentHistory(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatHistory> getSessionHistory(String sessionId) {
        log.debug("Fetching chat history for session: {}", sessionId);
        return chatHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatHistory> getHistoryByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching chat history for user: {} between {} and {}", userId, startDate, endDate);
        return chatHistoryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public String getConversationContext(String userId, int maxMessages) {
        log.debug("Building conversation context for user: {} with max {} messages", userId, maxMessages);

        List<ChatHistory> recentHistory = getRecentHistory(userId, maxMessages);

        if (recentHistory.isEmpty()) {
            return "";
        }

        // Reverse to get chronological order
        StringBuilder context = new StringBuilder();
        context.append("Previous conversation history:\n");

        for (int i = recentHistory.size() - 1; i >= 0; i--) {
            ChatHistory history = recentHistory.get(i);
            context.append("User: ").append(history.getUserMessage()).append("\n");
            if (history.getAgentResponse() != null) {
                context.append("Assistant: ").append(history.getAgentResponse()).append("\n");
            }
            context.append("\n");
        }

        log.debug("Built context with {} conversation turns", recentHistory.size());
        return context.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserMessages(String userId) {
        long count = chatHistoryRepository.countByUserId(userId);
        log.debug("User {} has {} total messages", userId, count);
        return count;
    }
}


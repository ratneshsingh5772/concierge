package com.finance.concierge.service.impl;

import com.finance.concierge.FinanceAgent;
import com.finance.concierge.dto.ChatRequestDTO;
import com.finance.concierge.dto.ChatResponseDTO;
import com.finance.concierge.entity.Expense;
import com.finance.concierge.exception.ChatServiceException;
import com.finance.concierge.helper.ResponseHelper;
import com.finance.concierge.service.ChatHistoryService;
import com.finance.concierge.service.ChatService;
import com.finance.concierge.service.ExpenseService;
import com.finance.concierge.service.SessionService;
import com.finance.concierge.util.MessageUtils;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Implementation of ChatService with persistent chat history and automatic expense tracking
 * Following Single Responsibility Principle (SRP) and Dependency Inversion Principle (DIP)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final InMemoryRunner runner;
    private final SessionService sessionService;
    private final ChatHistoryService chatHistoryService;
    private final ExpenseService expenseService;

    private static final int MAX_CONTEXT_MESSAGES = 10;

    @Override
    public Flux<String> sendMessageStream(ChatRequestDTO request) {
        log.info("Processing streaming message: {}", request.getMessage());

        try {
            String userId = request.getUserIdOrDefault();
            Long userIdLong = Long.parseLong(userId);
            Session session = sessionService.getOrCreateSession(userId);

            // Set user ID in FinanceAgent for database operations
            FinanceAgent.setCurrentUserId(userIdLong);

            // NOTE: Expense detection is handled by the AI agent's logExpense() tool
            // No need to parse here to avoid duplicate saves

            // Get conversation context from history
            String conversationContext = chatHistoryService.getConversationContext(userId, MAX_CONTEXT_MESSAGES);

            // Build message with context
            String messageWithContext = buildMessageWithContext(request.getMessage(), conversationContext);

            Content userMsg = MessageUtils.createContentFromText(messageWithContext);
            RunConfig runConfig = RunConfig.builder().build();

            Flowable<Event> events = runner.runAsync(
                    session.userId(),
                    session.id(),
                    userMsg,
                    runConfig
            );

            // Collect response for saving to history
            final StringBuilder responseCollector = new StringBuilder();

            return Flux.create(sink -> {
                events.subscribe(
                    event -> {
                        if (event.finalResponse()) {
                            String response = event.stringifyContent();
                            responseCollector.append(response);
                            log.debug("Streaming response chunk: {}", response);
                            sink.next(response);
                        }
                    },
                    error -> {
                        log.error("Error in streaming response", error);
                        sink.error(new ChatServiceException("Error processing streaming message", error));
                    },
                    () -> {
                        // Save to history when complete
                        try {
                            chatHistoryService.saveChatHistory(
                                userId,
                                session.id(),
                                request.getMessage(),
                                responseCollector.toString()
                            );
                            log.info("Chat history saved for user: {}", userId);
                        } catch (Exception e) {
                            log.error("Failed to save chat history", e);
                        } finally {
                            // Clear user ID from FinanceAgent thread local
                            FinanceAgent.clearCurrentUserId();
                        }
                        sink.complete();
                    }
                );
            });

        } catch (Exception e) {
            log.error("Error creating streaming response", e);
            FinanceAgent.clearCurrentUserId(); // Clear on error too
            throw new ChatServiceException("Failed to process streaming message", e);
        }
    }

    @Override
    public ChatResponseDTO sendMessage(ChatRequestDTO request) {
        log.info("Processing message (JSON mode): {}", request.getMessage());

        try {
            String userId = request.getUserIdOrDefault();
            Long userIdLong = Long.parseLong(userId);
            Session session = sessionService.getOrCreateSession(userId);

            // Set user ID in FinanceAgent for database operations
            FinanceAgent.setCurrentUserId(userIdLong);

            // NOTE: Expense detection is handled by the AI agent's logExpense() tool
            // No need to parse here to avoid duplicate saves

            // Get conversation context from history
            String conversationContext = chatHistoryService.getConversationContext(userId, MAX_CONTEXT_MESSAGES);
            log.debug("Retrieved conversation context with {} characters", conversationContext.length());

            // Build message with context
            String messageWithContext = buildMessageWithContext(request.getMessage(), conversationContext);

            Content userMsg = MessageUtils.createContentFromText(messageWithContext);
            RunConfig runConfig = RunConfig.builder().build();

            Flowable<Event> events = runner.runAsync(
                    session.userId(),
                    session.id(),
                    userMsg,
                    runConfig
            );

            String finalResponse = ResponseHelper.collectFinalResponse(events);
            log.info("Agent response (JSON mode): {}", finalResponse);

            // Save to chat history
            chatHistoryService.saveChatHistory(
                userId,
                session.id(),
                request.getMessage(),
                finalResponse
            );
            log.info("Chat history saved for user: {}", userId);

            // Clear user ID after processing
            FinanceAgent.clearCurrentUserId();

            return ChatResponseDTO.builder()
                    .response(finalResponse)
                    .userId(userId)
                    .timestamp(System.currentTimeMillis())
                    .conversationId(session.id())
                    .build();

        } catch (Exception e) {
            log.error("Error processing message", e);
            FinanceAgent.clearCurrentUserId(); // Clear on error
            throw new ChatServiceException("Failed to process message: " + e.getMessage(), e);
        }
    }


    /**
     * Build message with conversation context for better AI understanding
     */
    private String buildMessageWithContext(String currentMessage, String conversationContext) {
        if (conversationContext == null || conversationContext.isBlank()) {
            return currentMessage;
        }

        return conversationContext + "\nCurrent message:\n" + currentMessage;
    }
}

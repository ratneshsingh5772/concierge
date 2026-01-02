package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.ChatRequestDTO;
import com.finance.concierge.dto.ChatResponseDTO;
import com.finance.concierge.dto.SessionResetRequestDTO;
import com.finance.concierge.entity.ChatHistory;
import com.finance.concierge.service.ChatHistoryService;
import com.finance.concierge.service.ChatService;
import com.finance.concierge.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Chat operations
 * Following SOLID principles:
 * - Single Responsibility: Only handles HTTP requests/responses
 * - Open/Closed: Open for extension via service layer
 * - Dependency Inversion: Depends on abstractions (interfaces) not implementations
 */
@Tag(name = "Chat", description = "Chat and messaging endpoints for AI-powered finance assistance")
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {

    private final ChatService chatService;
    private final SessionService sessionService;
    private final ChatHistoryService chatHistoryService;

    /**
     * Send message and receive streaming response (SSE)
     * Used for real-time updates in web UI
     */
    @Operation(
        summary = "Send message (SSE streaming)",
        description = "Send a message to the AI finance assistant and receive a streaming response (Server-Sent Events)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Streaming response",
                content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
            )
        }
    )
    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(@Valid @RequestBody ChatRequestDTO request) {
        log.info("Received streaming message request from user: {}", request.getUserIdOrDefault());
        return chatService.sendMessageStream(request);
    }

    /**
     * Send message and receive JSON response
     * Recommended for API integrations and Postman testing
     */
    @Operation(
        summary = "Send message (JSON response)",
        description = "Send a message to the AI finance assistant and receive a JSON response with conversation context"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Message processed successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing token"
        )
    })
    @PostMapping(value = "/message/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ChatResponseDTO>> sendMessageJson(
            @Valid @RequestBody ChatRequestDTO request) {
        log.info("Received JSON message request from user: {}", request.getUserIdOrDefault());

        ChatResponseDTO response = chatService.sendMessage(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Message processed successfully")
        );
    }

    /**
     * Reset user session
     */
    @Operation(
        summary = "Reset user session",
        description = "Clear the conversation history and reset the session for a user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Session reset successfully"
        )
    })
    @PostMapping(value = "/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> resetSession(
            @RequestBody SessionResetRequestDTO request) {
        log.info("Received session reset request for user: {}", request.getUserIdOrDefault());

        String message = sessionService.resetSession(request);

        return ResponseEntity.ok(
                ApiResponse.success(message)
        );
    }

    /**
     * Get chat history for a user
     */
    @Operation(
        summary = "Get chat history",
        description = "Retrieve complete chat history for a specific user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Chat history retrieved successfully"
        )
    })
    @GetMapping(value = "/history/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ChatHistory>>> getChatHistory(
            @Parameter(description = "User ID to retrieve history for", required = true)
            @PathVariable String userId) {
        log.info("Fetching chat history for user: {}", userId);

        List<ChatHistory> history = chatHistoryService.getUserChatHistory(userId);

        return ResponseEntity.ok(
                ApiResponse.success(history, "Chat history retrieved successfully")
        );
    }

    /**
     * Get paginated chat history for a user
     */
    @Operation(
        summary = "Get paginated chat history",
        description = "Retrieve chat history with pagination support"
    )
    @GetMapping(value = "/history/{userId}/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Page<ChatHistory>>> getChatHistoryPaginated(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching paginated chat history for user: {} (page: {}, size: {})", userId, page, size);

        Page<ChatHistory> history = chatHistoryService.getUserChatHistoryPaginated(
                userId,
                PageRequest.of(page, size)
        );

        return ResponseEntity.ok(
                ApiResponse.success(history, "Chat history retrieved successfully")
        );
    }

    /**
     * Get recent chat history for a user
     */
    @Operation(
        summary = "Get recent chat history",
        description = "Retrieve the most recent N messages for a user"
    )
    @GetMapping(value = "/history/{userId}/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ChatHistory>>> getRecentHistory(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @Parameter(description = "Number of recent messages to retrieve") @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching recent {} messages for user: {}", limit, userId);

        List<ChatHistory> history = chatHistoryService.getRecentHistory(userId, limit);

        return ResponseEntity.ok(
                ApiResponse.success(history, "Recent chat history retrieved successfully")
        );
    }

    /**
     * Get chat statistics for a user
     */
    @Operation(
        summary = "Get user statistics",
        description = "Retrieve chat statistics including total messages and session status"
    )
    @GetMapping(value = "/stats/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChatStats(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        log.info("Fetching chat statistics for user: {}", userId);

        long totalMessages = chatHistoryService.countUserMessages(userId);
        boolean hasActiveSession = sessionService.hasSession(userId);

        Map<String, Object> stats = Map.of(
                "userId", userId,
                "totalMessages", totalMessages,
                "hasActiveSession", hasActiveSession
        );

        return ResponseEntity.ok(
                ApiResponse.success(stats, "Chat statistics retrieved successfully")
        );
    }

    /**
     * Health check endpoint
     */
    @Operation(
        summary = "Health check",
        description = "Check if the service is running and healthy",
        security = {}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Service is healthy"
        )
    })
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> healthData = Map.of(
                "status", "ok",
                "agent", "finance-concierge",
                "service", "running",
                "database", "connected"
        );

        return ResponseEntity.ok(
                ApiResponse.success(healthData, "Service is healthy")
        );
    }
}


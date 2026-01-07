package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.ChatRequestDTO;
import com.finance.concierge.dto.ChatResponseDTO;
import com.finance.concierge.dto.SessionResetRequestDTO;
import com.finance.concierge.entity.ChatHistory;
import com.finance.concierge.service.ChatHistoryService;
import com.finance.concierge.service.ChatService;
import com.finance.concierge.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SessionService sessionService;

    @Mock
    private ChatHistoryService chatHistoryService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;

    private ChatRequestDTO chatRequest;
    private ChatResponseDTO chatResponse;
    private SessionResetRequestDTO resetRequest;
    private List<ChatHistory> chatHistoryList;
    private Page<ChatHistory> chatHistoryPage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();

        chatRequest = ChatRequestDTO.builder()
                .userId("user123")
                .message("Hello, how can I track my expenses?")
                .build();

        chatResponse = ChatResponseDTO.builder()
                .userId("user123")
                .response("You can track expenses by using natural language input like 'I spent $15 on coffee'")
                .timestamp(System.currentTimeMillis())
                .conversationId("conv123")
                .build();

        resetRequest = SessionResetRequestDTO.builder()
                .userId("user123")
                .build();

        ChatHistory history1 = ChatHistory.builder()
                .id(1L)
                .userId("user123")
                .userMessage("Hello")
                .agentResponse("Hi there!")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        ChatHistory history2 = ChatHistory.builder()
                .id(2L)
                .userId("user123")
                .userMessage("How to track expenses?")
                .agentResponse("Use natural language input")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        chatHistoryList = Arrays.asList(history1, history2);
        chatHistoryPage = new PageImpl<>(chatHistoryList, PageRequest.of(0, 20), 2);
    }

    @Test
    void sendMessage_Success() throws Exception {
        // Given
        Flux<String> responseFlux = Flux.just("Hello", " ", "there", "!");
        when(chatService.sendMessageStream(any(ChatRequestDTO.class))).thenReturn(responseFlux);

        // When & Then
        mockMvc.perform(post("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": "user123",
                        "message": "Hello, how can I track my expenses?"
                    }
                    """))
                .andExpect(status().isOk());

        verify(chatService).sendMessageStream(any(ChatRequestDTO.class));
    }

    @Test
    void sendMessageJson_Success() throws Exception {
        // Given
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(chatResponse);

        // When & Then
        mockMvc.perform(post("/api/chat/message/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": "user123",
                        "message": "Hello, how can I track my expenses?"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Message processed successfully"))
                .andExpect(jsonPath("$.data.userId").value("user123"))
                .andExpect(jsonPath("$.data.conversationId").value("conv123"));

        verify(chatService).sendMessage(any(ChatRequestDTO.class));
    }

    @Test
    void sendMessageJson_InvalidInput() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/chat/message/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": "",
                        "message": ""
                    }
                    """))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).sendMessage(any(ChatRequestDTO.class));
    }

    @Test
    void resetSession_Success() throws Exception {
        // Given
        when(sessionService.resetSession(any(SessionResetRequestDTO.class)))
                .thenReturn("Session reset successfully for user: user123");

        // When & Then
        mockMvc.perform(post("/api/chat/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": "user123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Session reset successfully for user: user123"));

        verify(sessionService).resetSession(any(SessionResetRequestDTO.class));
    }

    @Test
    void getChatHistory_Success() throws Exception {
        // Given
        when(chatHistoryService.getUserChatHistory("user123")).thenReturn(chatHistoryList);

        // When & Then
        mockMvc.perform(get("/api/chat/history/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chat history retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].userMessage").value("Hello"))
                .andExpect(jsonPath("$.data[1].userMessage").value("How to track expenses?"));

        verify(chatHistoryService).getUserChatHistory("user123");
    }

    @Test
    void getChatHistory_Empty() throws Exception {
        // Given
        when(chatHistoryService.getUserChatHistory("user123")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/chat/history/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(chatHistoryService).getUserChatHistory("user123");
    }

    @Test
    void getChatHistoryPaginated_Success() throws Exception {
        // Given
        when(chatHistoryService.getUserChatHistoryPaginated(eq("user123"), any(PageRequest.class)))
                .thenReturn(chatHistoryPage);

        // When & Then
        mockMvc.perform(get("/api/chat/history/user123/paginated?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chat history retrieved successfully"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(chatHistoryService).getUserChatHistoryPaginated(eq("user123"), any(PageRequest.class));
    }

    @Test
    void getChatHistoryPaginated_CustomPageSize() throws Exception {
        // Given
        Page<ChatHistory> customPage = new PageImpl<>(chatHistoryList, PageRequest.of(1, 10), 12);
        when(chatHistoryService.getUserChatHistoryPaginated(eq("user123"), any(PageRequest.class)))
                .thenReturn(customPage);

        // When & Then
        mockMvc.perform(get("/api/chat/history/user123/paginated?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(12));

        verify(chatHistoryService).getUserChatHistoryPaginated(eq("user123"), any(PageRequest.class));
    }

    @Test
    void getRecentHistory_Success() throws Exception {
        // Given
        when(chatHistoryService.getRecentHistory("user123", 10)).thenReturn(chatHistoryList);

        // When & Then
        mockMvc.perform(get("/api/chat/history/user123/recent?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recent chat history retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(chatHistoryService).getRecentHistory("user123", 10);
    }

    @Test
    void getRecentHistory_DefaultLimit() throws Exception {
        // Given
        when(chatHistoryService.getRecentHistory("user123", 10)).thenReturn(chatHistoryList);

        // When & Then
        mockMvc.perform(get("/api/chat/history/user123/recent"))
                .andExpect(status().isOk());

        verify(chatHistoryService).getRecentHistory("user123", 10);
    }

    @Test
    void getChatStats_Success() throws Exception {
        // Given
        when(chatHistoryService.countUserMessages("user123")).thenReturn(25L);
        when(sessionService.hasSession("user123")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/chat/stats/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chat statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.userId").value("user123"))
                .andExpect(jsonPath("$.data.totalMessages").value(25))
                .andExpect(jsonPath("$.data.hasActiveSession").value(true));

        verify(chatHistoryService).countUserMessages("user123");
        verify(sessionService).hasSession("user123");
    }

    @Test
    void getChatStats_NoActiveSession() throws Exception {
        // Given
        when(chatHistoryService.countUserMessages("user123")).thenReturn(0L);
        when(sessionService.hasSession("user123")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/chat/stats/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalMessages").value(0))
                .andExpect(jsonPath("$.data.hasActiveSession").value(false));

        verify(chatHistoryService).countUserMessages("user123");
        verify(sessionService).hasSession("user123");
    }

    @Test
    void health_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ok"))
                .andExpect(jsonPath("$.data.agent").value("finance-concierge"))
                .andExpect(jsonPath("$.data.service").value("running"));
    }

    // Unit tests using direct controller method calls
    @Test
    void sendMessageJson_ControllerMethod_Success() {
        // Given
        when(chatService.sendMessage(any(ChatRequestDTO.class))).thenReturn(chatResponse);

        // When
        ResponseEntity<ApiResponse<ChatResponseDTO>> response = chatController.sendMessageJson(chatRequest);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Message processed successfully", response.getBody().getMessage());
        assertEquals(chatResponse, response.getBody().getData());
        verify(chatService).sendMessage(chatRequest);
    }

    @Test
    void resetSession_ControllerMethod_Success() {
        // Given
        when(sessionService.resetSession(any(SessionResetRequestDTO.class)))
                .thenReturn("Session reset successful");

        // When
        ResponseEntity<ApiResponse<String>> response = chatController.resetSession(resetRequest);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Session reset successful", response.getBody().getData());
        verify(sessionService).resetSession(resetRequest);
    }

    @Test
    void getChatHistory_ControllerMethod_Success() {
        // Given
        when(chatHistoryService.getUserChatHistory("user123")).thenReturn(chatHistoryList);

        // When
        ResponseEntity<ApiResponse<List<ChatHistory>>> response = chatController.getChatHistory("user123");

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Chat history retrieved successfully", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());
        verify(chatHistoryService).getUserChatHistory("user123");
    }

    @Test
    void getChatHistoryPaginated_ControllerMethod_Success() {
        // Given
        when(chatHistoryService.getUserChatHistoryPaginated(eq("user123"), any(PageRequest.class)))
                .thenReturn(chatHistoryPage);

        // When
        ResponseEntity<ApiResponse<Page<ChatHistory>>> response =
                chatController.getChatHistoryPaginated("user123", 0, 20);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(chatHistoryPage, response.getBody().getData());
        verify(chatHistoryService).getUserChatHistoryPaginated(eq("user123"), any(PageRequest.class));
    }

    @Test
    void getRecentHistory_ControllerMethod_Success() {
        // Given
        when(chatHistoryService.getRecentHistory("user123", 5)).thenReturn(chatHistoryList);

        // When
        ResponseEntity<ApiResponse<List<ChatHistory>>> response =
                chatController.getRecentHistory("user123", 5);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        verify(chatHistoryService).getRecentHistory("user123", 5);
    }

    @Test
    void getChatStats_ControllerMethod_Success() {
        // Given
        when(chatHistoryService.countUserMessages("user123")).thenReturn(10L);
        when(sessionService.hasSession("user123")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = chatController.getChatStats("user123");

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        Map<String, Object> stats = response.getBody().getData();
        assertEquals("user123", stats.get("userId"));
        assertEquals(10L, stats.get("totalMessages"));
        assertEquals(true, stats.get("hasActiveSession"));
        verify(chatHistoryService).countUserMessages("user123");
        verify(sessionService).hasSession("user123");
    }

    @Test
    void health_ControllerMethod_Success() {
        // When
        ResponseEntity<ApiResponse<Map<String, String>>> response = chatController.health();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        Map<String, String> healthData = response.getBody().getData();
        assertEquals("ok", healthData.get("status"));
        assertEquals("finance-concierge", healthData.get("agent"));
        assertEquals("running", healthData.get("service"));
    }

    @Test
    void sendMessage_ControllerMethod_Streaming() {
        // Given
        Flux<String> responseFlux = Flux.just("Response", " ", "chunk");
        when(chatService.sendMessageStream(any(ChatRequestDTO.class))).thenReturn(responseFlux);

        // When
        Flux<String> result = chatController.sendMessage(chatRequest);

        // Then
        assertNotNull(result);
        verify(chatService).sendMessageStream(chatRequest);
    }
}

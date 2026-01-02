package com.finance.concierge.service;

import com.finance.concierge.dto.ChatRequestDTO;
import com.finance.concierge.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

/**
 * Service interface for chat operations
 * Following Interface Segregation Principle (ISP) from SOLID
 */
public interface ChatService {

    /**
     * Send message and get streaming response
     *
     * @param request the chat request DTO
     * @return Flux of response strings for streaming
     */
    Flux<String> sendMessageStream(ChatRequestDTO request);

    /**
     * Send message and get complete JSON response
     *
     * @param request the chat request DTO
     * @return complete chat response DTO
     */
    ChatResponseDTO sendMessage(ChatRequestDTO request);
}


package com.finance.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for chat requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    private String userId;

    /**
     * Get userId with default fallback
     */
    public String getUserIdOrDefault() {
        return userId != null && !userId.isBlank() ? userId : "default-user";
    }
}


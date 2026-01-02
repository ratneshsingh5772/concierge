package com.finance.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for session reset requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResetRequestDTO {

    private String userId;

    /**
     * Get userId with default fallback
     */
    public String getUserIdOrDefault() {
        return userId != null && !userId.isBlank() ? userId : "default-user";
    }
}


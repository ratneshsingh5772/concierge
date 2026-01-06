package com.finance.concierge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Parsing Log entity for tracking AI parsing performance and debugging
 */
@Entity
@Table(name = "ai_parsing_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_success", columnList = "success")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiParsingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ai_log_user"))
    private User user;

    @Column(name = "original_message", nullable = false, columnDefinition = "TEXT")
    private String originalMessage;

    @Column(name = "parsed_amount", precision = 15, scale = 2)
    private BigDecimal parsedAmount;

    @Column(name = "parsed_category", length = 50)
    private String parsedCategory;

    @Column(name = "parsed_description", columnDefinition = "TEXT")
    private String parsedDescription;

    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "ai_response", columnDefinition = "JSON")
    private String aiResponse;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


package com.finance.concierge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Expense entity for tracking user expenses
 */
@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_expense_date", columnList = "expense_date"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_user_date", columnList = "user_id, expense_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_user"))
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_category"))
    private Category category;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_message", columnDefinition = "TEXT")
    private String originalMessage;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "ai_parsed", nullable = false)
    @Builder.Default
    private Boolean aiParsed = false;

    @Column(name = "ai_confidence", precision = 3, scale = 2)
    private BigDecimal aiConfidence;

    @Column(name = "ai_metadata", columnDefinition = "JSON")
    private String aiMetadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}


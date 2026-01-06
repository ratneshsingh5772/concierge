package com.finance.concierge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for user budget settings
 * Stores category-wise and total budget limits
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category_id", "budget_period"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(name = "budget_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "budget_period", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BudgetPeriod budgetPeriod;

    @Column(name = "is_total_budget", nullable = false)
    @Builder.Default
    private Boolean isTotalBudget = false;

    @Column(name = "alert_threshold", precision = 5, scale = 2)
    private BigDecimal alertThreshold; // Alert when X% of budget is used

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Budget period enum
     */
    public enum BudgetPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}

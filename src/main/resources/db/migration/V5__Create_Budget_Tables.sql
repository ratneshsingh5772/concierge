-- Budget Management Tables
-- V5__Create_Budget_Tables.sql

-- Drop the existing budgets table if it exists
DROP TABLE IF EXISTS budgets;

-- Create budgets table with correct structure
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    budget_amount DECIMAL(10, 2) NOT NULL,
    budget_period VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    is_total_budget BOOLEAN NOT NULL DEFAULT FALSE,
    alert_threshold DECIMAL(5, 2) NULL COMMENT 'Alert when X% of budget is used',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,

    -- Ensure only one budget per user-category-period combination
    UNIQUE KEY uk_budget_user_category_period (user_id, category_id, budget_period),

    -- Index for faster lookups
    INDEX idx_budget_user_active (user_id, is_active),
    INDEX idx_budget_category (category_id),
    INDEX idx_budget_period (budget_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User budget settings for categories and total spending';

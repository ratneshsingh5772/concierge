-- Create categories table (predefined categories)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon VARCHAR(50),
    color VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_categories_name (name),
    INDEX idx_categories_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default categories
INSERT INTO categories (name, description, icon, color) VALUES
('Food', 'Food and beverages including groceries, restaurants, coffee', '🍔', '#FF6B6B'),
('Transport', 'Transportation costs including fuel, public transport, taxi', '🚗', '#4ECDC4'),
('Bills', 'Utility bills, rent, subscriptions, insurance', '📄', '#45B7D1'),
('Entertainment', 'Movies, games, hobbies, events', '🎮', '#FFA07A'),
('Shopping', 'Clothing, electronics, household items', '🛍️', '#98D8C8'),
('Health', 'Medical expenses, pharmacy, fitness', '💊', '#F7B801'),
('Education', 'Courses, books, training', '📚', '#6C5CE7'),
('Other', 'Miscellaneous expenses', '📦', '#95A5A6');

-- Create expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    original_message TEXT,
    expense_date DATE NOT NULL,
    ai_parsed BOOLEAN NOT NULL DEFAULT FALSE,
    ai_confidence DECIMAL(3, 2),
    ai_metadata JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_expenses_user_id (user_id),
    INDEX idx_expenses_category_id (category_id),
    INDEX idx_expenses_expense_date (expense_date),
    INDEX idx_expenses_created_at (created_at),
    INDEX idx_expenses_user_date (user_id, expense_date),
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create budgets table
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    period_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL DEFAULT 'MONTHLY',
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_active (is_active),
    INDEX idx_user_category (user_id, category_id),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_category_period UNIQUE (user_id, category_id, period_type, start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create expense_tags table for additional categorization
CREATE TABLE IF NOT EXISTS expense_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_expense_id (expense_id),
    INDEX idx_tag_name (tag_name),
    CONSTRAINT fk_tag_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create ai_parsing_logs table for tracking AI performance
CREATE TABLE IF NOT EXISTS ai_parsing_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_message TEXT NOT NULL,
    parsed_amount DECIMAL(15, 2),
    parsed_category VARCHAR(50),
    parsed_description TEXT,
    confidence_score DECIMAL(3, 2),
    ai_model VARCHAR(50),
    ai_response JSON,
    processing_time_ms INT,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_logs_user_id (user_id),
    INDEX idx_ai_logs_created_at (created_at),
    INDEX idx_ai_logs_success (success),
    CONSTRAINT fk_ai_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


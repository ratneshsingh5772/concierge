-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    last_login DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add user_id foreign key to user_sessions table
ALTER TABLE user_sessions ADD COLUMN user_id_fk BIGINT;
ALTER TABLE user_sessions ADD CONSTRAINT fk_user_session_user FOREIGN KEY (user_id_fk) REFERENCES users(id) ON DELETE CASCADE;

-- Add user_id foreign key to chat_history table
ALTER TABLE chat_history ADD COLUMN user_id_fk BIGINT;
ALTER TABLE chat_history ADD CONSTRAINT fk_chat_history_user FOREIGN KEY (user_id_fk) REFERENCES users(id) ON DELETE CASCADE;

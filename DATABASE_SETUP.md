# MySQL Database Setup Guide

## Overview

This application now uses **MySQL** database for persistent storage of:
- User sessions (survives application restarts)
- Complete chat history (provides context to Google Gemini AI)
- Historical data for better AI responses

## Database Schema

### Database Name
```
concierge
```

### Tables

#### 1. user_sessions
Stores user session information for persistence across app restarts.

```sql
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    session_id VARCHAR(255) NOT NULL,
    app_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_activity DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_active (is_active),
    INDEX idx_last_activity (last_activity)
);
```

#### 2. chat_history
Stores complete conversation history for context and analysis.

```sql
CREATE TABLE chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    user_message TEXT NOT NULL,
    agent_response TEXT,
    message_type VARCHAR(20) DEFAULT 'CHAT',
    metadata TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_created (user_id, created_at)
);
```

## Installation Steps

### Step 1: Install MySQL (if not already installed)

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

**macOS (using Homebrew):**
```bash
brew install mysql
brew services start mysql
```

**Windows:**
Download and install from: https://dev.mysql.com/downloads/mysql/

### Step 2: Secure MySQL Installation

```bash
sudo mysql_secure_installation
```

Follow the prompts to:
- Set root password
- Remove anonymous users
- Disallow root login remotely
- Remove test database

### Step 3: Create Database and User

Login to MySQL:
```bash
sudo mysql -u root -p
```

Or without sudo (if you set password):
```bash
mysql -u root -p
```

Create database and user:
```sql
-- Create database
CREATE DATABASE IF NOT EXISTS concierge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (replace 'your_password' with a strong password)
CREATE USER IF NOT EXISTS 'concierge_user'@'localhost' IDENTIFIED BY 'your_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON concierge.* TO 'concierge_user'@'localhost';

-- Apply privileges
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES;

-- Exit
EXIT;
```

### Step 4: Update Application Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/concierge?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=concierge_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Flyway Configuration (handles database migrations)
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

**Using root user (for development only):**
```properties
spring.datasource.username=root
spring.datasource.password=
```

### Step 5: Build and Run

The application will automatically create tables using Flyway migrations on first run:

```bash
# Using Java 21 helper script
./run-java21.sh clean install

# Run the application
./run-java21.sh
```

On startup, you should see:
```
Flyway migration executed successfully
Database schema created: user_sessions, chat_history
```

## Verification

### 1. Check Database Connection

```bash
mysql -u concierge_user -p concierge
```

```sql
-- Show tables
SHOW TABLES;

-- Expected output:
-- +---------------------+
-- | Tables_in_concierge |
-- +---------------------+
-- | chat_history        |
-- | flyway_schema_history |
-- | user_sessions       |
-- +---------------------+

-- Describe tables
DESCRIBE user_sessions;
DESCRIBE chat_history;
```

### 2. Test the Application

**Start the application:**
```bash
./run-java21.sh
```

**Send a test message:**
```bash
curl -X POST http://localhost:8081/api/chat/message/json \
  -H "Content-Type: application/json" \
  -d '{"message": "I spent $30 on groceries", "userId": "testuser"}'
```

**Check database:**
```sql
-- Check session created
SELECT * FROM user_sessions WHERE user_id = 'testuser';

-- Check chat history
SELECT * FROM chat_history WHERE user_id = 'testuser';
```

### 3. View Chat History via API

```bash
# Get all chat history
curl http://localhost:8081/api/chat/history/testuser

# Get recent 5 messages
curl http://localhost:8081/api/chat/history/testuser/recent?limit=5

# Get chat statistics
curl http://localhost:8081/api/chat/stats/testuser
```

## Features Enabled

### 1. Persistent Sessions
- Sessions survive application restarts
- Users can continue conversations after server restart
- Last activity tracking

### 2. Complete Chat History
- All conversations stored permanently
- Provides context to Google Gemini AI
- Better, more contextual responses

### 3. Historical Context for AI
The AI now receives the last 10 messages as context, enabling:
- References to previous expenses
- Better understanding of user patterns
- Continuity in conversations

### 4. New API Endpoints

**Get Chat History:**
```bash
GET /api/chat/history/{userId}
GET /api/chat/history/{userId}/recent?limit=10
GET /api/chat/history/{userId}/paginated?page=0&size=20
```

**Get Statistics:**
```bash
GET /api/chat/stats/{userId}
```

## Configuration Options

### application.properties

```properties
# Enable SQL logging (for debugging)
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG

# Disable Flyway (if you want manual schema management)
spring.flyway.enabled=false

# Change Hibernate DDL mode
# - validate: Only validate schema (production)
# - update: Auto-update schema (development)
# - create: Create fresh schema on startup
# - create-drop: Create and drop on shutdown
spring.jpa.hibernate.ddl-auto=validate
```

### Connection Pool Settings

Add to application.properties for production:

```properties
# HikariCP connection pool settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

## Maintenance

### Backup Database

```bash
# Backup database
mysqldump -u concierge_user -p concierge > concierge_backup.sql

# Restore from backup
mysql -u concierge_user -p concierge < concierge_backup.sql
```

### Clean Old Chat History

```sql
-- Delete chat history older than 90 days
DELETE FROM chat_history 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### Monitor Database Size

```sql
-- Check table sizes
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'concierge'
ORDER BY (data_length + index_length) DESC;
```

## Troubleshooting

### Issue 1: Cannot connect to database

**Error:**
```
Communications link failure
```

**Solution:**
```bash
# Check MySQL is running
sudo systemctl status mysql

# Start MySQL if not running
sudo systemctl start mysql

# Check connection
mysql -u root -p
```

### Issue 2: Access denied

**Error:**
```
Access denied for user 'concierge_user'@'localhost'
```

**Solution:**
```sql
-- Grant privileges again
GRANT ALL PRIVILEGES ON concierge.* TO 'concierge_user'@'localhost';
FLUSH PRIVILEGES;
```

### Issue 3: Flyway migration fails

**Error:**
```
Flyway migration failed
```

**Solution:**
```sql
-- Check Flyway history
SELECT * FROM flyway_schema_history;

-- Repair Flyway if needed
-- Add to application.properties temporarily:
spring.flyway.repair=true
```

### Issue 4: Table doesn't exist

**Solution:**
```bash
# Delete Flyway history and recreate
mysql -u root -p concierge -e "DROP TABLE flyway_schema_history;"

# Restart application (Flyway will recreate tables)
./run-java21.sh
```

## Production Recommendations

1. **Use dedicated MySQL user (not root)**
2. **Enable SSL for connections**
3. **Set up regular backups**
4. **Monitor database performance**
5. **Index optimization for large datasets**
6. **Connection pooling properly configured**
7. **Regular maintenance and cleanup**

## Environment Variables (Production)

Instead of hardcoding credentials in application.properties:

```bash
# Set environment variables
export DB_URL="jdbc:mysql://localhost:3306/concierge"
export DB_USERNAME="concierge_user"
export DB_PASSWORD="your_secure_password"
```

Update application.properties:
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## Summary

✅ **MySQL database configured**
✅ **Persistent user sessions**
✅ **Complete chat history stored**
✅ **Historical context provided to AI**
✅ **Automatic schema management via Flyway**
✅ **New API endpoints for history access**
✅ **Production-ready with proper indexing**

Your application now maintains all user data persistently and provides rich historical context to Google Gemini AI for better, more contextual responses!


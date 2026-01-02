#!/bin/bash
# Quick Database Setup Script for Finance Concierge

echo "==================================================="
echo "Finance Concierge - MySQL Database Setup"
echo "==================================================="
echo ""

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo "❌ MySQL is not installed."
    echo "Please install MySQL first:"
    echo "  Ubuntu/Debian: sudo apt install mysql-server"
    echo "  macOS: brew install mysql"
    exit 1
fi

echo "✓ MySQL is installed"
echo ""

# Check if MySQL is running
if ! sudo systemctl is-active --quiet mysql 2>/dev/null && ! pgrep -x mysqld > /dev/null; then
    echo "⚠ MySQL is not running. Attempting to start..."
    sudo systemctl start mysql 2>/dev/null || echo "Please start MySQL manually"
fi

echo "==================================================="
echo "Database Configuration"
echo "==================================================="
echo ""
echo "Please enter MySQL root password (press Enter if no password):"
read -s MYSQL_ROOT_PASSWORD

# Test connection
if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
    MYSQL_CMD="mysql -u root"
else
    MYSQL_CMD="mysql -u root -p$MYSQL_ROOT_PASSWORD"
fi

# Check if connection works
if ! $MYSQL_CMD -e "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ Cannot connect to MySQL. Please check your root password."
    exit 1
fi

echo "✓ Connected to MySQL"
echo ""

# Create database and user
echo "Creating database 'concierge'..."

$MYSQL_CMD << EOF
CREATE DATABASE IF NOT EXISTS concierge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'concierge_user'@'localhost' IDENTIFIED BY 'concierge_pass';
GRANT ALL PRIVILEGES ON concierge.* TO 'concierge_user'@'localhost';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo "✓ Database 'concierge' created successfully"
    echo "✓ User 'concierge_user' created with password 'concierge_pass'"
else
    echo "❌ Failed to create database or user"
    exit 1
fi

echo ""
echo "==================================================="
echo "Updating application.properties"
echo "==================================================="
echo ""

# Update application.properties
PROPS_FILE="src/main/resources/application.properties"

if [ -f "$PROPS_FILE" ]; then
    # Backup original file
    cp "$PROPS_FILE" "$PROPS_FILE.backup"

    # Update database credentials
    sed -i 's/spring.datasource.username=.*/spring.datasource.username=concierge_user/' "$PROPS_FILE"
    sed -i 's/spring.datasource.password=.*/spring.datasource.password=concierge_pass/' "$PROPS_FILE"

    echo "✓ application.properties updated"
    echo "  - Username: concierge_user"
    echo "  - Password: concierge_pass"
    echo "  - Backup saved to: $PROPS_FILE.backup"
else
    echo "⚠ application.properties not found at $PROPS_FILE"
fi

echo ""
echo "==================================================="
echo "Verifying Setup"
echo "==================================================="
echo ""

# Verify database exists
DB_EXISTS=$(mysql -u concierge_user -pconcierge_pass -e "SHOW DATABASES LIKE 'concierge';" 2>/dev/null | grep concierge)

if [ -n "$DB_EXISTS" ]; then
    echo "✓ Database 'concierge' verified"
else
    echo "❌ Database verification failed"
    exit 1
fi

echo ""
echo "==================================================="
echo "Setup Complete!"
echo "==================================================="
echo ""
echo "Database Information:"
echo "  - Database: concierge"
echo "  - Host: localhost:3306"
echo "  - Username: concierge_user"
echo "  - Password: concierge_pass"
echo ""
echo "Next Steps:"
echo "  1. Run the application: ./run-java21.sh"
echo "  2. The application will create tables automatically using Flyway"
echo "  3. Test with: curl http://localhost:8081/api/chat/health"
echo ""
echo "To verify database tables after first run:"
echo "  mysql -u concierge_user -pconcierge_pass concierge -e 'SHOW TABLES;'"
echo ""
echo "==================================================="


#!/bin/bash
# Complete setup script: database + environment + run backend

set -e  # Exit on error

echo "======================================"
echo "AI Chat Application - Complete Setup"
echo "======================================"
echo ""

# Step 1: Reset database (drop and recreate)
echo "[1/3] Resetting MySQL database (fresh start)..."
sudo mysql << 'EOF'
DROP DATABASE IF EXISTS aichat_db;
DROP USER IF EXISTS 'aichat_user'@'localhost';
CREATE DATABASE aichat_db;
CREATE USER 'aichat_user'@'localhost' IDENTIFIED BY 'aichat_pass';
GRANT ALL PRIVILEGES ON aichat_db.* TO 'aichat_user'@'localhost';
FLUSH PRIVILEGES;
SELECT 'Database reset complete!' AS '';
EOF

if [ $? -eq 0 ]; then
    echo "✓ Database setup successful"
else
    echo "✗ Database setup failed"
    exit 1
fi

echo ""

# Step 2: Load environment variables
echo "[2/3] Loading environment variables..."
cd "$(dirname "$0")"
if [ -f .env ]; then
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
    echo "✓ Environment variables loaded from .env"
else
    echo "✗ Warning: .env file not found"
fi

echo ""

# Step 3: Run backend
echo "[3/3] Starting backend server..."
echo "Press Ctrl+C to stop the server"
echo ""

mvn spring-boot:run -DskipTests

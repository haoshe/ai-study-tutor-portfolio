## For easier database setup
#!/bin/bash

echo "Setting up MySQL database for AI Chat..."

sudo mysql << EOF
CREATE DATABASE IF NOT EXISTS aichat_db;
CREATE USER IF NOT EXISTS 'aichat_user'@'localhost' IDENTIFIED BY 'aichat_pass';
GRANT ALL PRIVILEGES ON aichat_db.* TO 'aichat_user'@'localhost';
FLUSH PRIVILEGES;
SELECT 'Database setup complete!' AS '';
EOF

echo "Done! You can now run: mvn spring-boot:run"
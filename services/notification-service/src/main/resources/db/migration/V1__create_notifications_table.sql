-- Flyway Migration: Create notifications table
-- Version: 1
-- Description: Create initial notifications table schema

CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,

                                             recipient_id BIGINT NOT NULL COMMENT 'User ID who receives the notification',

                                             type VARCHAR(50) NOT NULL COMMENT 'Type of notification (APPLICATION_SUBMITTED, etc.)',

    channel VARCHAR(20) NOT NULL COMMENT 'Delivery channel (IN_APP, EMAIL, BOTH)',

    title VARCHAR(255) NOT NULL COMMENT 'Notification title/subject',

    message LONGTEXT NOT NULL COMMENT 'Notification message body',

    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether notification has been read',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When notification was created',

    sent_at TIMESTAMP NULL COMMENT 'When email was sent (if applicable)',

    -- Indexes for performance
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read),
    INDEX idx_recipient_created (recipient_id, created_at)

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='Notification records for RevHire platform';
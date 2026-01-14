-- =========================================================
-- Flyway Migration: V9__create_notifications_table.sql
-- 설명: 알림 발송 관리 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

CREATE TABLE NOTIFICATIONS (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    notification_type ENUM('EMAIL','SMS') NOT NULL,
    notification_status ENUM('PENDING','SENT','FAILED','RETRY') NOT NULL,

    recipient VARCHAR(255) NOT NULL COMMENT 'AES-256',
    content TEXT NOT NULL,

    retry_count INT NOT NULL DEFAULT 0,
    scheduled_at TIMESTAMP NULL,
    sent_at TIMESTAMP NULL,
    error_message TEXT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    CONSTRAINT chk_retry_count CHECK (retry_count BETWEEN 0 AND 3),

    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_status_scheduled (notification_status, scheduled_at)
) ENGINE=InnoDB;

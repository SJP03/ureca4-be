-- ============================================================================
-- V9: 알림 발송 이력 테이블 생성
-- 목적: 이메일/SMS 알림 발송 로그 및 재시도 관리
-- ============================================================================

CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '알림 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    bill_id BIGINT NOT NULL COMMENT '청구 ID',
    notification_type ENUM('EMAIL', 'SMS') NOT NULL COMMENT '알림 유형',
    notification_status ENUM('PENDING', 'WAITING', 'SENT', 'FAILED', 'RETRY') NOT NULL DEFAULT 'PENDING' COMMENT '발송 상태',
    recipient VARCHAR(255) NOT NULL COMMENT '수신자 (암호화)',
    content TEXT NULL COMMENT '알림 내용',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    scheduled_at TIMESTAMP NULL COMMENT '발송 예정 시각 (금지시간대 관리)',
    sent_at TIMESTAMP NULL COMMENT '발송 완료 시각',
    error_message TEXT NULL COMMENT '에러 메시지',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    
    -- 외래키
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    
    -- 재시도 횟수 제한
    CONSTRAINT chk_notifications_retry CHECK (retry_count BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='알림 발송 이력';

-- 중복 발송 방지
ALTER TABLE notifications ADD CONSTRAINT uk_notifications_bill_type UNIQUE (bill_id, notification_type);

-- 인덱스
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_bill ON notifications(bill_id);
CREATE INDEX idx_notifications_status ON notifications(notification_status);
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_at);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_status_scheduled ON notifications(notification_status, scheduled_at);
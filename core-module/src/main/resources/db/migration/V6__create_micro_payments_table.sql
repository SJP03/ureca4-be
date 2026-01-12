-- ============================================================================
-- V6: 소액결제 테이블 생성
-- 목적: 콘텐츠, 앱 등 소액결제 이력 500만 건 관리
-- ============================================================================

CREATE TABLE micro_payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '소액결제 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    amount INT NOT NULL COMMENT '결제 금액 (원)',
    merchant_name VARCHAR(200) NOT NULL COMMENT '가맹점명',
    payment_type ENUM('APP', 'CONTENT', 'GAME') NOT NULL COMMENT '결제 유형',
    payment_date TIMESTAMP NOT NULL COMMENT '결제일시',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    
    -- 외래키
    CONSTRAINT fk_micro_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='소액결제 내역';

-- 인덱스
CREATE INDEX idx_micro_payments_subscription ON micro_payments(subscription_id);
CREATE INDEX idx_micro_payments_date ON micro_payments(payment_date);
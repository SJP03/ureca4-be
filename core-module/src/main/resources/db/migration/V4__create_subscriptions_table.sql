-- ============================================================================
-- V4: 구독 테이블 생성
-- 목적: 사용자별 요금제 가입 및 변경 이력
-- ============================================================================

CREATE TABLE subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '구독 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    plan_id BIGINT NOT NULL COMMENT '요금제 ID',
    subscription_start_date DATE NOT NULL COMMENT '가입일 (일할 계산 기준일)',
    subscription_end_date DATE NULL COMMENT '해지일 (NULL이면 현재 사용 중)',
    subscription_status ENUM('ACTIVE', 'SUSPENDED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    
    -- 외래키
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES plans(plan_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='구독 정보';

-- 인덱스
CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_plan ON subscriptions(plan_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(subscription_status);
CREATE INDEX idx_subscriptions_start_date ON subscriptions(subscription_start_date);
CREATE INDEX idx_subscriptions_user_status ON subscriptions(user_id, subscription_status);
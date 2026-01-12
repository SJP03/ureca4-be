-- ============================================================================
-- V5: 구독 부가서비스 테이블 생성
-- 목적: 사용자별 부가서비스 가입 정보
-- ============================================================================

CREATE TABLE subscription_addons (
    subscription_addon_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '구독 부가서비스 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    addon_id BIGINT NOT NULL COMMENT '부가서비스 ID',
    added_date DATE NOT NULL COMMENT '추가일 (일할 계산 기준일)',
    removed_date DATE NULL COMMENT '해지일 (NULL이면 현재 사용 중)',
    status ENUM('ACTIVE', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    
    -- 외래키
    CONSTRAINT fk_subscription_addons_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_addons_addon FOREIGN KEY (addon_id) REFERENCES addons(addon_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='구독 부가서비스';

-- 인덱스
CREATE INDEX idx_subscription_addons_subscription ON subscription_addons(subscription_id);
CREATE INDEX idx_subscription_addons_addon ON subscription_addons(addon_id);
CREATE INDEX idx_subscription_addons_status ON subscription_addons(status);
-- ============================================================================
-- V3: 부가서비스 테이블 생성
-- 목적: LG U+ 부가서비스 마스터 데이터 (7종)
-- ============================================================================

CREATE TABLE addons (
    addon_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '부가서비스 ID',
    addon_category VARCHAR(50) NOT NULL COMMENT '카테고리 (영상/음악, 통화/편의, 안심/보험)',
    addon_name VARCHAR(100) NOT NULL COMMENT '서비스명',
    monthly_fee INT NOT NULL COMMENT '월정액 (원)',
    description VARCHAR(200) NULL COMMENT '설명',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='부가서비스 정보';

-- 유니크 제약
CREATE UNIQUE INDEX uk_addons_name ON addons(addon_name);

-- 인덱스
CREATE INDEX idx_addons_category ON addons(addon_category);
CREATE INDEX idx_addons_active ON addons(is_active);
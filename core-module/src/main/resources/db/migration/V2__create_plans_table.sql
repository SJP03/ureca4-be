-- ============================================================================
-- V2: 요금제 테이블 생성
-- 목적: LG U+ 실제 요금제 마스터 데이터 (13종)
-- ============================================================================

CREATE TABLE plans (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '요금제 ID',
    plan_category VARCHAR(50) NOT NULL COMMENT '구분 (5G프리미엄, 5G스탠다드, 5G실속형, LTE, 키즈/청소년)',
    plan_name VARCHAR(100) NOT NULL COMMENT '요금제명',
    monthly_fee INT NOT NULL COMMENT '월정액 (원)',
    data_limit VARCHAR(50) NOT NULL COMMENT '데이터 제공량',
    voice_limit VARCHAR(50) NOT NULL COMMENT '음성통화 제공량',
    sms_limit VARCHAR(50) NOT NULL COMMENT '문자 제공량',
    plan_type ENUM('5G', 'LTE') NOT NULL COMMENT '타입',
    description VARCHAR(200) NULL COMMENT '비고',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='요금제 정보';

-- 유니크 제약
CREATE UNIQUE INDEX uk_plans_name ON plans(plan_name);

-- 인덱스
CREATE INDEX idx_plans_category ON plans(plan_category);
CREATE INDEX idx_plans_type ON plans(plan_type);
CREATE INDEX idx_plans_active ON plans(is_active);
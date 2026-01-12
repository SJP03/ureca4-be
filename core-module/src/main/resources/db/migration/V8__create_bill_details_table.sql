-- ============================================================================
-- V8: 청구 상세 내역 테이블 생성
-- 목적: 청구서의 세부 항목별 내역 (일할 계산 포함)
-- ============================================================================

CREATE TABLE bill_details (
    bill_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '청구 상세 ID',
    bill_id BIGINT NOT NULL COMMENT '청구 ID',
    detail_type ENUM('PLAN', 'ADDON', 'MICRO_PAYMENT') NOT NULL COMMENT '항목 타입',
    reference_id BIGINT NOT NULL COMMENT '참조 ID (plan_id, addon_id, payment_id)',
    item_name VARCHAR(200) NOT NULL COMMENT '항목명',
    amount INT NOT NULL COMMENT '금액 (원)',
    usage_days INT NULL COMMENT '사용 일수 (일할 계산 시 사용)',
    total_days INT NULL COMMENT '해당월 총일수 (일할 계산 시 사용)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    
    -- 외래키
    CONSTRAINT fk_bill_details_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='청구 상세 내역';

-- 인덱스
CREATE INDEX idx_bill_details_bill ON bill_details(bill_id);
CREATE INDEX idx_bill_details_type ON bill_details(detail_type);
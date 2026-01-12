-- ============================================================================
-- V7: 청구서 테이블 생성
-- 목적: 월별 청구 집계 데이터 (배치 결과)
-- ============================================================================

CREATE TABLE bills (
    bill_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '청구 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    bill_year_month CHAR(6) NOT NULL COMMENT '청구년월 (YYYYMM)',
    plan_fee INT NOT NULL COMMENT '요금제 금액 (일할 계산 적용)',
    addon_fee INT NOT NULL DEFAULT 0 COMMENT '부가서비스 금액 (일할 계산 적용)',
    micro_payment_fee INT NOT NULL DEFAULT 0 COMMENT '소액결제 금액',
    total_amount INT NOT NULL COMMENT '총 청구 금액',
    bill_date DATE NOT NULL COMMENT '청구일',
    due_date DATE NOT NULL COMMENT '납부기한',
    bill_status ENUM('PENDING', 'SENT', 'PAID', 'OVERDUE') NOT NULL DEFAULT 'PENDING' COMMENT '청구 상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    
    -- 외래키
    CONSTRAINT fk_bills_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_bills_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='청구서 정보';

-- 유니크 제약 (중복 청구 방지)
ALTER TABLE bills ADD CONSTRAINT uk_bills_user_month UNIQUE (user_id, bill_year_month);

-- 인덱스
CREATE INDEX idx_bills_user ON bills(user_id);
CREATE INDEX idx_bills_year_month ON bills(bill_year_month);
CREATE INDEX idx_bills_status ON bills(bill_status);
CREATE INDEX idx_bills_date ON bills(bill_date);
CREATE INDEX idx_bills_user_year_month ON bills(user_id, bill_year_month);
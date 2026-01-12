-- ============================================================================
-- V10: 배치 실행 이력 테이블 생성
-- 목적: 정산 배치 작업 실행 관리 및 중복 실행 방지
-- ============================================================================

CREATE TABLE batch_executions (
    execution_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '배치 실행 ID',
    batch_name VARCHAR(100) NOT NULL COMMENT '배치명 (BILLING_BATCH)',
    execution_year_month CHAR(6) NOT NULL COMMENT '실행 년월 (YYYYMM)',
    execution_status ENUM('RUNNING', 'COMPLETED', 'FAILED') NOT NULL COMMENT '실행 상태',
    total_users INT NOT NULL DEFAULT 0 COMMENT '대상 사용자 수',
    processed_users INT NOT NULL DEFAULT 0 COMMENT '처리된 사용자 수',
    failed_users INT NOT NULL DEFAULT 0 COMMENT '실패 사용자 수',
    start_time TIMESTAMP NOT NULL COMMENT '시작 시각',
    end_time TIMESTAMP NULL COMMENT '종료 시각',
    duration_seconds INT NULL COMMENT '소요시간 (초)',
    error_message TEXT NULL COMMENT '에러 메시지',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='배치 실행 이력';

-- 유니크 제약 (중복 실행 방지)
ALTER TABLE batch_executions ADD CONSTRAINT uk_batch_year_month 
UNIQUE (batch_name, execution_year_month);

-- 인덱스
CREATE INDEX idx_batch_executions_year_month ON batch_executions(execution_year_month);
CREATE INDEX idx_batch_executions_status ON batch_executions(execution_status);
CREATE INDEX idx_batch_executions_start_time ON batch_executions(start_time);
-- ============================================================================
-- V11: 배치 처리 상세 테이블 생성
-- 목적: 사용자별 배치 처리 결과 추적
-- ============================================================================

CREATE TABLE batch_execution_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '배치 히스토리 ID',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    process_status ENUM('SUCCESS', 'FAILED', 'SKIPPED') NOT NULL COMMENT '처리 상태',
    error_detail TEXT NULL COMMENT '에러 상세',
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '처리 시각',
    
    -- 외래키
    CONSTRAINT fk_batch_history_execution FOREIGN KEY (execution_id) REFERENCES batch_executions(execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_batch_history_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='배치 처리 상세';

-- 인덱스
CREATE INDEX idx_batch_history_execution ON batch_execution_history(execution_id);
CREATE INDEX idx_batch_history_user ON batch_execution_history(user_id);
CREATE INDEX idx_batch_history_status ON batch_execution_history(process_status);
CREATE INDEX idx_batch_history_execution_user ON batch_execution_history(execution_id, user_id);
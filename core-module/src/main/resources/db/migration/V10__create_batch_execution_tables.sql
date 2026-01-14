-- =========================================================
-- Flyway Migration: V10__create_batch_execution_tables.sql
-- 설명: 배치 실행 관리 및 이력 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

-- BATCH_EXECUTIONS: 배치 실행 관리
CREATE TABLE BATCH_EXECUTIONS (
    execution_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_name VARCHAR(100) NOT NULL,
    y_month CHAR(6) NOT NULL,

    execution_status ENUM('RUNNING','COMPLETED','FAILED') NOT NULL,
    total_users INT NOT NULL DEFAULT 0,
    processed_users INT NOT NULL DEFAULT 0,
    failed_users INT NOT NULL DEFAULT 0,

    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NULL,
    duration_seconds INT NULL,
    error_message TEXT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_batch_execution UNIQUE (batch_name, y_month),

    INDEX idx_batch_execution_status (execution_status),
    INDEX idx_batch_execution_year_month (y_month)
) ENGINE=InnoDB;

-- BATCH_EXECUTION_HISTORY: 배치 실행 이력
CREATE TABLE BATCH_EXECUTION_HISTORY (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    process_status ENUM('SUCCESS','FAILED','SKIPPED') NOT NULL,
    error_detail TEXT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_batch_history_execution
        FOREIGN KEY (execution_id) REFERENCES BATCH_EXECUTIONS(execution_id),

    CONSTRAINT fk_batch_history_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    INDEX idx_batch_history_execution_user (execution_id, user_id),
    INDEX idx_batch_history_status (process_status)
) ENGINE=InnoDB;

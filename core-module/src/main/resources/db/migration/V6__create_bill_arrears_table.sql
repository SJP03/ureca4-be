-- =========================================================
-- Flyway Migration: V6__create_bill_arrears_table.sql
-- 설명: 체납 요금 관리 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

CREATE TABLE BILL_ARREARS (
    arrears_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,
    bill_id BIGINT NOT NULL,

    arrears_amount INT NOT NULL,
    arrears_status ENUM('UNPAID','PARTIAL','PAID') NOT NULL,

    due_date DATE NOT NULL,
    paid_date DATE NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_arrears_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    CONSTRAINT fk_arrears_bill
        FOREIGN KEY (bill_id) REFERENCES BILLS(bill_id),

    INDEX idx_arrears_user_status (user_id, arrears_status)
) ENGINE=InnoDB;

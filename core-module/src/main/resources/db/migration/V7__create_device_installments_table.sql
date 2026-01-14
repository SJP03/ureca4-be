-- =========================================================
-- Flyway Migration: V7__create_device_installments_table.sql
-- 설명: 단말 할부 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

CREATE TABLE DEVICE_INSTALLMENTS (
    installment_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,
    device_name VARCHAR(100) NOT NULL,

    original_price INT NOT NULL,
    installment_principal INT NOT NULL,
    monthly_fee INT NOT NULL,

    total_months INT NOT NULL,
    remaining_months INT NOT NULL,

    status ENUM('ONGOING','COMPLETED') NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_installments_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    INDEX idx_installments_user_status (user_id, status)
) ENGINE=InnoDB;

-- =========================================================
-- Flyway Migration: V3__create_billing_tables.sql
-- 설명: 청구서 및 청구 상세 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

-- BILLS: 월별 청구서
CREATE TABLE BILLS (
    bill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bills_user 
        FOREIGN KEY (user_id) REFERENCES USERS(user_id)
) ENGINE=InnoDB;

-- BILL_DETAILS: 청구서 상세 내역
CREATE TABLE BILL_DETAILS (
    detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    detail_type VARCHAR(50) NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bill_details_bill
        FOREIGN KEY (bill_id) REFERENCES BILLS(bill_id)
) ENGINE=InnoDB;

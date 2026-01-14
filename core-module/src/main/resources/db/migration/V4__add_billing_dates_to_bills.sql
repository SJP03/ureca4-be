-- =========================================================
-- Flyway Migration: V4__add_billing_dates_to_bills.sql
-- 설명: BILLS 테이블에 정산일/청구일 컬럼 추가
-- 작성일: 2025-01-15
-- =========================================================

ALTER TABLE BILLS
ADD COLUMN settlement_date DATE NOT NULL COMMENT '정산 기준일',
ADD COLUMN bill_issue_date DATE NOT NULL COMMENT '청구서 발행일';

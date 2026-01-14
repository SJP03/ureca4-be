-- =========================================================
-- Flyway Migration: V5__add_charge_category_to_bill_details.sql
-- 설명: BILL_DETAILS 테이블 확장 (정산 원장화)
-- 작성일: 2025-01-15
-- =========================================================

ALTER TABLE BILL_DETAILS
ADD COLUMN charge_category ENUM(
    'BASE_FEE',
    'DEVICE_FEE',
    'ADDON_FEE',
    'DISCOUNT',
    'MICRO_PAYMENT'
) NOT NULL AFTER detail_type,

ADD COLUMN related_user_id BIGINT NULL COMMENT '본인/자녀/워치 사용자',

ADD CONSTRAINT fk_bill_details_related_user
    FOREIGN KEY (related_user_id) REFERENCES USERS(user_id);

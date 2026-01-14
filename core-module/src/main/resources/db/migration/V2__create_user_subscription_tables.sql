-- =========================================================
-- Flyway Migration: V2__create_user_subscription_tables.sql
-- 설명: 사용자 요금제/부가서비스 가입 및 소액결제 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

-- USER_PLANS: 사용자 요금제 가입
CREATE TABLE USER_PLANS (
    user_plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NULL,

    status ENUM('ACTIVE','CHANGED','CANCELLED') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_plans_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    CONSTRAINT fk_user_plans_plan
        FOREIGN KEY (plan_id) REFERENCES PLANS(plan_id),

    INDEX idx_user_plans_user_status (user_id, status),
    INDEX idx_user_plans_period (start_date, end_date)
) ENGINE=InnoDB;

-- USER_ADDONS: 사용자 부가서비스 가입
CREATE TABLE USER_ADDONS (
    user_addon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    addon_id BIGINT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NULL,

    status ENUM('ACTIVE','CANCELLED') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_addons_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    CONSTRAINT fk_user_addons_addon
        FOREIGN KEY (addon_id) REFERENCES ADDONS(addon_id),

    INDEX idx_user_addons_user_status (user_id, status)
) ENGINE=InnoDB;

-- MICRO_PAYMENTS: 소액결제 내역
CREATE TABLE MICRO_PAYMENTS (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    amount INT NOT NULL,
    merchant_name VARCHAR(200) NOT NULL,
    payment_type ENUM('APP','CONTENT','GAME') NOT NULL,
    payment_date TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_micro_payments_user
        FOREIGN KEY (user_id) REFERENCES USERS(user_id),

    INDEX idx_micro_payments_user (user_id),
    INDEX idx_micro_payments_date (payment_date)
) ENGINE=InnoDB;

-- =========================================================
-- Flyway Migration: V1__create_user_and_product_tables.sql
-- 설명: 기본 사용자, 요금제, 부가서비스 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

-- USERS: 사용자 기본 정보
CREATE TABLE USERS (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT 'AES-256',
    phone VARCHAR(100) NOT NULL UNIQUE COMMENT 'AES-256',
    name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    status ENUM('ACTIVE','SUSPENDED','TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_status (status),
    INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB;

-- PLANS: 요금제 마스터
CREATE TABLE PLANS (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_name VARCHAR(100) NOT NULL UNIQUE,
    plan_category VARCHAR(50) NOT NULL,
    plan_type ENUM('5G','LTE') NOT NULL,
    monthly_fee INT NOT NULL,
    data_limit VARCHAR(50) NOT NULL,
    voice_limit VARCHAR(50) NOT NULL,
    sms_limit VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_plans_category (plan_category),
    INDEX idx_plans_type (plan_type),
    INDEX idx_plans_active (is_active)
) ENGINE=InnoDB;

-- ADDONS: 부가서비스 마스터
CREATE TABLE ADDONS (
    addon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    addon_name VARCHAR(100) NOT NULL UNIQUE,
    addon_category VARCHAR(50) NOT NULL,
    monthly_fee INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_addons_category (addon_category),
    INDEX idx_addons_active (is_active)
) ENGINE=InnoDB;

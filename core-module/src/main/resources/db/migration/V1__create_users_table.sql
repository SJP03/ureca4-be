-- ============================================================================
-- V1: 사용자 테이블 생성
-- 목적: 통신 서비스 가입자 100만 명 기본 정보 관리
-- ============================================================================

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
    email VARCHAR(255) NOT NULL COMMENT '이메일 주소 (AES-256 암호화)',
    phone VARCHAR(100) NOT NULL COMMENT '휴대폰 번호 (AES-256 암호화)',
    name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
    birth_date DATE NOT NULL COMMENT '생년월일',
    user_status ENUM('ACTIVE', 'SUSPENDED', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 정보';

-- 유니크 제약
CREATE UNIQUE INDEX uk_users_email ON users(email);
CREATE UNIQUE INDEX uk_users_phone ON users(phone);

-- 인덱스
CREATE INDEX idx_users_status ON users(user_status);
CREATE INDEX idx_users_created_at ON users(created_at);
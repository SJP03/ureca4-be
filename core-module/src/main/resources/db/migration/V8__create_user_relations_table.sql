-- =========================================================
-- Flyway Migration: V8__create_user_relations_table.sql
-- 설명: 가족 관계 (본인/자녀/워치) 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================

CREATE TABLE USER_RELATIONS (
    relation_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    parent_user_id BIGINT NOT NULL,
    child_user_id BIGINT NOT NULL,

    relation_type ENUM('SELF','CHILD','WATCH','FAMILY') NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_relation_parent
        FOREIGN KEY (parent_user_id) REFERENCES USERS(user_id),

    CONSTRAINT fk_relation_child
        FOREIGN KEY (child_user_id) REFERENCES USERS(user_id),

    INDEX idx_relations_parent (parent_user_id),
    INDEX idx_relations_child (child_user_id)
) ENGINE=InnoDB;

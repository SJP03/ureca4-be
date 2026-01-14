-- =========================================================
-- Flyway Migration: V11_alter_plan_and_drop_batch_execution_tables.sql
-- 설명: 배치 실행 관리 및 이력 테이블 생성
-- 작성일: 2025-01-15
-- =========================================================
-- PLANS: 요금제 마스터
ALTER TABLE PLANS
	DROP COLUMN sms_limit;

-- BATCH_*: spring batch 실행 시 생성되는 테이블 drop
DROP TABLE IF EXISTS BATCH_EXECUTION_HISTORY;
DROP TABLE IF EXISTS BATCH_EXECUTIONS;
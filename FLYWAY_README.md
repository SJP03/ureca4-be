# LG U+ 청구 시스템 - Flyway 마이그레이션 가이드

## 📋 마이그레이션 버전 구조

```
src/main/resources/db/migration/
├── V1__create_user_and_product_tables.sql       # 사용자, 요금제, 부가서비스 마스터
├── V2__create_user_subscription_tables.sql      # 가입 및 소액결제 내역
├── V3__create_billing_tables.sql                # 청구서 기본 테이블
├── V4__add_billing_dates_to_bills.sql           # 청구서 정산일/청구일 추가
├── V5__add_charge_category_to_bill_details.sql  # 청구 상세 확장 (정산 원장화)
├── V6__create_bill_arrears_table.sql            # 체납 관리
├── V7__create_device_installments_table.sql     # 단말 할부
├── V8__create_user_relations_table.sql          # 가족 관계
├── V9__create_notifications_table.sql           # 알림 시스템
└── V10__create_batch_execution_tables.sql       # 배치 실행 관리
```

## 🎯 마이그레이션 전략

### 1단계: 기본 도메인 (V1-V2)
- V1: 마스터 데이터 (사용자, 요금제, 부가서비스)
- V2: 가입 및 이용 데이터 (요금제 가입, 부가서비스 가입, 소액결제)

### 2단계: 청구 시스템 (V3-V5)
- V3: 청구서 기본 구조
- V4: 청구서 확장 - 정산일/청구일 분리
- V5: 청구 상세 확장 - 정산 원장화 (charge_category, related_user_id)

### 3단계: 부가 기능 (V6-V8)
- V6: 체납 관리
- V7: 단말 할부
- V8: 가족 관계 (본인/자녀/워치)

### 4단계: 시스템 지원 (V9-V10)
- V9: 알림 발송 시스템
- V10: 배치 실행 관리

## 🚀 사용 방법

### 1. Gradle 의존성 추가

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
    runtimeOnly 'com.mysql:mysql-connector-j'
}
```

### 2. application.yml 설정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lg_uplus_billing_v2
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    
  jpa:
    hibernate:
      ddl-auto: validate
```

### 3. 마이그레이션 파일 배치

모든 SQL 파일을 `src/main/resources/db/migration/` 디렉토리에 복사

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

## 📊 마이그레이션 실행 순서

```
V1  → USERS, PLANS, ADDONS 생성
V2  → USER_PLANS, USER_ADDONS, MICRO_PAYMENTS 생성
V3  → BILLS, BILL_DETAILS 생성
V4  → BILLS 테이블에 settlement_date, bill_issue_date 추가
V5  → BILL_DETAILS 테이블에 charge_category, related_user_id 추가
V6  → BILL_ARREARS 생성
V7  → DEVICE_INSTALLMENTS 생성
V8  → USER_RELATIONS 생성
V9  → NOTIFICATIONS 생성
V10 → BATCH_EXECUTIONS, BATCH_EXECUTION_HISTORY 생성
```

## ✅ 마이그레이션 확인

### 성공 로그 예시
```
Flyway Community Edition
Database: jdbc:mysql://localhost:3306/lg_uplus_billing_v2
Successfully validated 10 migrations
Current version of schema `lg_uplus_billing_v2`: 10
Schema `lg_uplus_billing_v2` is up to date. No migration necessary.
```

### 마이그레이션 히스토리 확인
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## 🔄 롤백 전략

Flyway Community Edition은 자동 롤백을 지원하지 않습니다.
롤백이 필요한 경우:

1. **수동 롤백**: 각 버전에 대응하는 UNDO 스크립트 작성
2. **백업 복구**: 마이그레이션 전 DB 백업 활용
3. **Flyway Teams**: 자동 롤백 기능 사용 (유료)

## 📝 버전 관리 규칙

- **V숫자__설명.sql** 형식 준수
- 한 번 적용된 마이그레이션은 **절대 수정 금지**
- 변경이 필요한 경우 **새로운 버전** 추가
- 각 버전은 **독립적으로 실행 가능**해야 함

## 🎨 마이그레이션 설계 원칙

1. **도메인별 분리**: 관련 테이블을 함께 묶어서 관리
2. **점진적 확장**: 기본 구조 → 확장 기능 순서
3. **의존성 고려**: FK 참조 순서에 맞게 배치
4. **명확한 설명**: 각 버전의 목적을 주석으로 명시

## 🔧 트러블슈팅

### 문제: "Table already exists" 오류
**해결**: `spring.flyway.baseline-on-migrate: true` 설정

### 문제: 체크섬 불일치
**해결**: 마이그레이션 파일 수정 금지, 새 버전으로 변경 적용

### 문제: FK 제약조건 오류
**해결**: 마이그레이션 순서 확인, 참조 테이블이 먼저 생성되는지 체크

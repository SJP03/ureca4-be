
# 🚀 Billing System – Team Onboarding Guide

## ✅ 1. 프로젝트 개요

이 프로젝트는 **Spring Boot 기반 멀티 모듈 구조**로 구성된 Billing System 입니다.
각 모듈 간 역할을 분리하고 재사용성을 높이기 위해 아래와 같은 구조를 사용합니다.

---

## 📁 2. 프로젝트 구조

```
project-root
 ┣ settings.gradle
 ┣ build.gradle
 ┣ .env.example
 ┣ core-module
 ┃ ┗ build.gradle
 ┣ admin-module
 ┃ ┗ build.gradle
 ┗ api-module
   ┗ build.gradle
```

### 🔎 모듈 역할

| 모듈           | 역할            |
| ------------ | ------------- |
| core-module  | 공통 도메인, 공통 설정 |
| api-module   | Public API 서버 |
| admin-module | 관리자 서버        |

---

## 🧩 3. 멀티 모듈 설정

### 📌 settings.gradle

루트 경로에 있어야 하며 모듈을 등록합니다.

```gradle
rootProject.name = "billing-system"

include("core-module")
include("admin-module")
include("api-module")
```

---

### 📌 Root build.gradle (공통 설정)

```gradle
subprojects {
    apply plugin: 'java'

    group = 'com.ureca'
    version = '1.0.0'

    repositories {
        mavenCentral()
    }

    test {
        useJUnitPlatform()
    }
}
```

---

## 🛠 4. 개발 환경

| 항목    | 요구사항               |
| ----- | ------------------ |
| JDK   | 17 (혹은 프로젝트 설정 기준) |
| DB    | MySQL              |
| Cache | Redis              |
| Build | Gradle             |

---

# 🔐 5. .env 사용 가이드

### ✅ 1) `.env.example` 복사

프로젝트 루트에 존재하는 파일:

```
.env.example
```

이를 복사하여 `.env` 파일 생성

```
cp .env.example .env
```

---

### ✅ 2) `.env` 내용 작성 예시

```
DB_URL=jdbc:mysql://localhost:3306/billing?serverTimezone=UTC&characterEncoding=UTF-8
DB_USERNAME=root
DB_PASSWORD=1234

REDIS_HOST=localhost
REDIS_PORT=6379
```

---

### ✅ 3) Spring Boot 에서 환경변수 적용 방식

`application.yml` 또는 `properties` 내부에서 이렇게 사용:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
```

📌 **중요**

* `Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${DB_URL}` 같은 에러는
  `.env` 가 적용 안 됐거나 변수 값이 비어있을 때 발생함
* `.env` 반드시 존재해야 함

---

# ▶️ 6. 프로젝트 실행 방법

### 1️⃣ Clone

```
git clone <repo-url>
```

### 2️⃣ 반드시 “루트 폴더 기준으로” 프로젝트 열기

IntelliJ 기준:

```
project-root 선택 → Open as Project
```

### 3️⃣ `.env` 파일 생성 & 값 채우기

### 4️⃣ 빌드

```
./gradlew clean build
```

### 5️⃣ 실행

```
./gradlew bootRun
```

또는 IDE Run

---

# 📘 7. Swagger API 문서

### 기본 접속 경로

```
http://localhost:8080/api/swagger-ui.html
```

> `server.servlet.context-path=/api` 설정이 적용된 경우 위 경로가 기본입니다.

---

### 3. 모듈별 실행
```bash
# Admin API (8080)
./gradlew :admin-module:bootRun

# Batch Service (8081)
./gradlew :batch-module:bootRun

# Notification Service (8082)
./gradlew :notification-module:bootRun
```

## API 테스트
```bash
# Hello World
curl http://localhost:8080/api/hello

# 헬스체크
curl http://localhost:8080/api/health/all

# Swagger UI
http://localhost:8080/api/swagger-ui.html
```


---

# 🧪 8. 헬스체크

서버 정상 여부 확인

```
http://localhost:8080/api/actuator/health
```

Expected:

```
status: UP
```

---

# 💬 9. Trouble Shooting

| 문제                       | 원인        | 해결                        |
| ------------------------ | --------- | ------------------------- |
| MySQL 연결 실패              | .env 미작성  | .env 채우기                  |
| Redis DOWN               | Redis 미실행 | Redis 실행                  |
| Swagger 404              | 경로 오류     | `/api/swagger-ui.html` 확인 |
| Driver claims jdbcUrl 오류 | 환경변수 미적용  | .env 존재 여부 확인             |

---

## 팀원

- 조장: 윤재영
- 조원: 권태환, 신우철, 박성준, 이윤경

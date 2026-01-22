# 🐳 Ureca Team 4 - Docker 배포 가이드

이 문서는 로컬 개발 환경에서 Docker Compose를 사용하여 전체 인프라와 애플리케이션을 실행하는 방법을 안내합니다.

## 1. 사전 준비 (Prerequisites)
* **Docker Desktop** 설치 완료
* 프로젝트 루트 디렉토리에 `.env.docker` 파일 존재 여부 확인
    * *없을 경우 팀 공유 드라이브나 슬랙에서 다운로드해 주세요.*

## 2. 파일 구조 설명
| 파일명 | 설명 |
| :--- | :--- |
| `docker-compose.infra.yml` | **인프라**: MySQL, Redis, Kafka, Zookeeper, Kafka UI |
| `docker-compose.app.yml` | **앱**: Admin, Notification, Batch (상시 실행), Core-Job (일회성) |
| `.env.docker` | DB 계정, 포트, 암호화 키 등 환경변수 설정 파일 |

---

## 3. 전체 실행 및 종료

### ✅ 전체 시스템 실행 (Infrastructure + Apps)
인프라(DB, Kafka)와 상시 실행되는 서버(Admin, Batch, Notification)를 모두 띄웁니다.
```bash
# 백그라운드 실행
docker compose -f docker-compose.infra.yml -f docker-compose.app.yml up -d

# (코드 수정 시) 이미지를 새로 빌드하며 실행
docker compose -f docker-compose.infra.yml -f docker-compose.app.yml up -d --build
```

### 🛑 전체 종료 (Shutdown)
컨테이너를 정지하고 네트워크를 제거합니다.
```bash
docker compose -f docker-compose.infra.yml -f docker-compose.app.yml down
```

---

## 4. 🛠 더미 데이터 생성 (Batch Job 수동 실행)

`core-module`을 이용한 초기 데이터 생성은 **필요할 때만 명령어로 실행**합니다.
작업이 완료되면 컨테이너는 자동으로 삭제(`--rm`)됩니다.

### 1) User 및 기초 데이터 생성 (가장 먼저 실행)
```bash
docker compose -f docker-compose.infra.yml -f docker-compose.app.yml run --rm core-job java -jar /app/app.jar --spring.batch.job.name=userDummyDataJob
```

### 2) 월별 청구 데이터 생성
* `targetYearMonth` 파라미터 필수 (예: `2025-08`)
```bash
docker compose -f docker-compose.infra.yml -f docker-compose.app.yml run --rm core-job java -jar /app/app.jar --spring.batch.job.name=monthlyDummyDataJob targetYearMonth=2025-08
  ```

---

## 5. 🌐 접속 정보 (Port Mapping)

로컬에서 접속할 때는 아래의 **Local Port**를 사용하세요.

| 서비스 | Internal Port | **Local Port** | 접속 URL / 정보 |
| :--- | :---: | :---: | :--- |
| **MySQL** | 3306 | **3307** | `jdbc:mysql://localhost:3307/urecaTeam4_db` |
| **Redis** | 6379 | **6379** | localhost:6379 |
| **Kafka** | 9092 | **29092** | localhost:29092 (외부 접속용) |
| **Kafka UI** | 8080 | **28080** | [http://localhost:28080](http://localhost:28080) |
| **Admin Module** | 8080 | **8081** | [http://localhost:8081](http://localhost:8081) |
| **Notification** | 8080 | **8082** | [http://localhost:8082](http://localhost:8082) |
| **Batch Module** | 8080 | **8083** | [http://localhost:8083](http://localhost:8083) |

---

## 6. 🚨 트러블슈팅 (FAQ)

**Q. 로그를 실시간으로 보고 싶어요.**
```bash
# 전체 로그 확인
docker compose -f docker-compose.infra.yml -f docker-compose.app.yml logs -f

# 특정 서비스 로그만 확인 (예: batch-module)
docker logs -f lgubill-batch
```

**Q. DB를 완전히 초기화하고 싶어요.**
DB 데이터가 꼬였을 때, 컨테이너를 내린 후 볼륨을 삭제하세요.
```bash
docker volume rm ureca4-be_mysql_data
```

**Q. Kafka 메시지가 제대로 들어갔는지 확인하고 싶어요.**
1. [http://localhost:28080](http://localhost:28080) 접속
2. **Topics** 메뉴 클릭
3. 해당 토픽 선택 -> **Messages** 탭 클릭 -> `Execute` 버튼 클릭

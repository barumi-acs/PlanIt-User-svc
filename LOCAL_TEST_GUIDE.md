# 로컬 테스트 가이드 (Docker 없이)

## 빠른 시작

### 1단계: 스크립트 실행

```powershell
cd PlanIt-User-svc
.\run-local.ps1
```

이 스크립트는 자동으로:
- MariaDB와 Redis를 Docker로 시작
- 환경 변수 설정
- 실행 방법 안내

### 2단계: 애플리케이션 실행

#### 방법 A: IntelliJ IDEA (권장)

1. IntelliJ IDEA에서 `PlanIt-User-svc` 프로젝트 열기
2. `src/main/java/com/planit/userservice/UserServiceApplication.java` 찾기
3. 우클릭 > `Run 'UserServiceApplication'`

#### 방법 B: Gradle 명령어

```powershell
.\gradlew.bat bootRun
```

#### 방법 C: JAR 파일 실행

```powershell
# JAR 빌드
.\gradlew.bat bootJar

# 실행
java -jar build\libs\planit-base-template-0.0.1-SNAPSHOT.jar
```

### 3단계: 테스트

```powershell
# 헬스체크
curl http://localhost:8080/actuator/health

# 카테고리 조회
curl http://localhost:8080/api/v1/users/categories

# Swagger UI
# 브라우저에서 http://localhost:8080/swagger-ui/index.html 접속
```

## 수동 설정 (스크립트 없이)

### 1. MariaDB와 Redis 시작

```powershell
cd PlanIt-User-svc
docker-compose -f docker-compose.local.yml up -d mariadb redis
```

### 2. 환경 변수 설정

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mariadb://localhost:3306/planit_user_db"
$env:SPRING_DATASOURCE_USERNAME = "planit_user"
$env:SPRING_DATASOURCE_PASSWORD = "planit_password"
$env:SPRING_DATA_REDIS_HOST = "localhost"
$env:SPRING_DATA_REDIS_PORT = "6379"
$env:JWT_SECRET = "your-secret-key-change-in-production-must-be-at-least-256-bits-long-for-hs256-algorithm"
$env:AWS_REGION = "ap-northeast-2"
$env:AWS_COGNITO_USER_POOL_ID = "dummy-pool-id"
$env:AWS_CREDENTIALS_ACCESS_KEY = "dummy-access-key"
$env:AWS_CREDENTIALS_SECRET_KEY = "dummy-secret-key"
```

### 3. 애플리케이션 실행

```powershell
.\gradlew.bat bootRun
```

## IntelliJ IDEA 설정

### Run Configuration 생성

1. `Run` > `Edit Configurations...`
2. `+` 버튼 클릭 > `Spring Boot` 선택
3. 다음 설정 입력:

```
Name: UserServiceApplication
Main class: com.planit.userservice.UserServiceApplication
Environment variables:
  SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/planit_user_db;
  SPRING_DATASOURCE_USERNAME=planit_user;
  SPRING_DATASOURCE_PASSWORD=planit_password;
  SPRING_DATA_REDIS_HOST=localhost;
  SPRING_DATA_REDIS_PORT=6379;
  JWT_SECRET=your-secret-key-change-in-production-must-be-at-least-256-bits-long-for-hs256-algorithm;
  AWS_REGION=ap-northeast-2;
  AWS_COGNITO_USER_POOL_ID=dummy-pool-id;
  AWS_CREDENTIALS_ACCESS_KEY=dummy-access-key;
  AWS_CREDENTIALS_SECRET_KEY=dummy-secret-key
```

4. `Apply` > `OK`
5. `Run` 버튼 클릭 또는 `Shift+F10`

## API 테스트

### 1. 헬스체크

```powershell
curl http://localhost:8080/actuator/health
```

**응답:**
```json
{
  "status": "UP"
}
```

### 2. 카테고리 조회 (인증 불필요)

```powershell
curl http://localhost:8080/api/v1/users/categories
```

**응답:**
```json
{
  "status": 200,
  "message": "카테고리 조회 성공",
  "data": [
    {
      "categoryId": 1,
      "name": "운동",
      "colorHex": "#FF6B6B",
      "description": "건강과 체력 향상을 위한 운동 활동"
    }
  ]
}
```

### 3. 약관 조회 (인증 불필요)

```powershell
curl http://localhost:8080/api/v1/users/terms
```

### 4. Swagger UI

브라우저에서 접속:
```
http://localhost:8080/swagger-ui/index.html
```

모든 API 엔드포인트를 확인하고 테스트할 수 있습니다.

## Frontend 연동 테스트

### 1. Frontend 환경 변수 설정

```powershell
cd ..\PlanIt-FE
```

`.env.development` 파일 생성:
```env
VITE_USER_SERVICE_URL=http://localhost:8080
VITE_SCHEDULE_SERVICE_URL=http://localhost:3002
VITE_INTELLIGENCE_SERVICE_URL=http://localhost:3003
VITE_INSIGHT_SERVICE_URL=http://localhost:3004
VITE_GEMINI_API_KEY=your_api_key_here
```

### 2. Frontend 실행

```powershell
npm install
npm run dev
```

### 3. 브라우저 접속

```
http://localhost:3000
```

## 문제 해결

### MariaDB 연결 실패

**증상:** `Communications link failure`

**해결:**
```powershell
# MariaDB 컨테이너 상태 확인
docker ps | findstr mariadb

# 로그 확인
docker logs planit-user-db

# 재시작
docker-compose -f docker-compose.local.yml restart mariadb
```

### Redis 연결 실패

**증상:** `Unable to connect to Redis`

**해결:**
```powershell
# Redis 컨테이너 상태 확인
docker ps | findstr redis

# Redis 연결 테스트
docker exec -it planit-redis redis-cli ping
# 응답: PONG

# 재시작
docker-compose -f docker-compose.local.yml restart redis
```

### 포트 충돌

**증상:** `Port 8080 is already in use`

**해결:**
```powershell
# 포트 사용 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료 (관리자 권한 필요)
taskkill /PID <PID번호> /F
```

### Gradle 빌드 실패

**증상:** `JAVA_HOME is set to an invalid directory`

**해결:**
IntelliJ IDEA를 사용하면 이 문제를 우회할 수 있습니다.

또는:
```powershell
# Java 버전 확인
java -version

# JAVA_HOME 확인
echo $env:JAVA_HOME

# IntelliJ에서 Gradle JVM 설정
# Settings > Build, Execution, Deployment > Build Tools > Gradle
# Gradle JVM: Project SDK 선택
```

## 종료 방법

### 1. Spring Boot 애플리케이션 종료

- IntelliJ: Stop 버튼 클릭
- 터미널: `Ctrl+C`

### 2. Docker 컨테이너 종료

```powershell
# 컨테이너 중지
docker-compose -f docker-compose.local.yml down

# 데이터까지 삭제
docker-compose -f docker-compose.local.yml down -v
```

## 로그 확인

### Spring Boot 로그

- IntelliJ: Run 창에서 실시간 확인
- 파일: `logs/planit.log`

### Docker 로그

```powershell
# MariaDB 로그
docker logs -f planit-user-db

# Redis 로그
docker logs -f planit-redis
```

## 데이터베이스 접속

### MariaDB 접속

```powershell
# Docker 컨테이너로 접속
docker exec -it planit-user-db mariadb -u planit_user -p
# 비밀번호: planit_password

# 데이터베이스 확인
USE planit_user_db;
SHOW TABLES;
SELECT * FROM users;
```

### Redis 접속

```powershell
# Docker 컨테이너로 접속
docker exec -it planit-redis redis-cli

# 키 확인
KEYS *

# 특정 키 조회
GET refresh_token:user_id
```

## 성능 모니터링

### Actuator 엔드포인트

```powershell
# 헬스체크
curl http://localhost:8080/actuator/health

# 메트릭
curl http://localhost:8080/actuator/metrics

# 환경 변수
curl http://localhost:8080/actuator/env
```

## 다음 단계

1. ✅ 로컬 환경에서 Backend 실행 완료
2. ✅ API 테스트 완료
3. ⬜ Frontend 연동 테스트
4. ⬜ 통합 테스트
5. ⬜ Docker 배포 준비

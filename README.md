# PlanIt User Service

PlanIt의 사용자 인증, 프로필 관리, 친구 관리를 담당하는 마이크로서비스입니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.11
- Spring Data JPA
- MariaDB
- Redis
- AWS Cognito
- JWT (jjwt 0.12.3)
- UUID v7
- Lombok

## 주요 기능

### 인증 (Authentication)
- 회원가입 (Cognito 연동)
- 로그인 (Cognito ID Token 검증)
- 계정 삭제 (Soft Delete)
- JWT 토큰 발급 및 검증

### 프로필 (Profile)
- 프로필 수정 (닉네임, 관심 카테고리)
- 유저 검색 (닉네임 기반)

### 친구 (Friends)
- 친구 요청 처리 (수락/거절)
- 받은 친구 요청 목록 조회
- 친구 목록 조회
- 친구 삭제

### 메타데이터 (Metadata)
- 관심 카테고리 조회 (8대 카테고리)
- 약관 목록 조회

## API 엔드포인트

### 인증 API
- `POST /api/v1/users/auth/signup` - 회원가입
- `POST /api/v1/users/auth/login` - 로그인
- `DELETE /api/v1/users/auth/withdraw` - 계정 삭제

### 프로필 API
- `PUT /api/v1/users/profile` - 프로필 수정
- `GET /api/v1/users/search` - 유저 검색

### 친구 API
- `POST /api/v1/users/friends/requests` - 친구 요청 처리
- `GET /api/v1/users/friends/requests/received` - 받은 친구 요청 목록
- `GET /api/v1/users/friends` - 친구 목록
- `DELETE /api/v1/users/friends/{friendshipId}` - 친구 삭제

### 메타데이터 API
- `GET /api/v1/users/categories` - 카테고리 목록 (인증 불필요)
- `GET /api/v1/users/terms` - 약관 목록 (인증 불필요)

## 환경 설정

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/planit_user_db
    username: root
    password: password
  
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-secret-key-here
  access-token-validity: 900000  # 15분
  refresh-token-validity: 604800000  # 7일

aws:
  region: ap-northeast-2
  cognito:
    user-pool-id: your-user-pool-id
  credentials:
    access-key: your-access-key
    secret-key: your-secret-key
```

## 실행 방법

### 1. 데이터베이스 준비
```bash
# MariaDB 실행
mysql -u root -p

# 데이터베이스 생성
CREATE DATABASE planit_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Redis 실행
```bash
redis-server
```

### 3. 애플리케이션 실행
```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

## 데이터베이스 스키마

### users
- user_id (PK, UUID v7)
- nickname (UNIQUE)
- email (UNIQUE)
- cognito_sub (UNIQUE)
- is_retention_agreed
- created_at, updated_at, deleted_at

### terms
- term_id (PK, AUTO_INCREMENT)
- title, content, version
- is_required, type

### user_agreements
- agreement_id (PK, AUTO_INCREMENT)
- user_id (FK), term_id (FK)
- agreed_at

### interest_category
- category_id (PK, AUTO_INCREMENT)
- name, color_hex, description

### user_interest
- interest_id (PK, AUTO_INCREMENT)
- user_id (FK), category_id (FK)
- created_at, deleted_at

### friends
- friendship_id (PK, AUTO_INCREMENT)
- requester_id (FK), approver_id (FK)
- status (PENDING, ACCEPTED, REJECTED)
- created_at, updated_at, deleted_at

## 보안

- JWT 기반 인증
- AWS Cognito 연동
- Refresh Token은 Redis에 저장
- Soft Delete 패턴 적용
- CORS 설정

## 포트

- HTTP: 8080
- gRPC: 9090

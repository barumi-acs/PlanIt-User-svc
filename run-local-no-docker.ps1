# PlanIt User Service 로컬 실행 스크립트 (Docker 없이)

Write-Host "=== PlanIt User Service 로컬 실행 (Docker 없이) ===" -ForegroundColor Green
Write-Host "로컬 MariaDB/MySQL만 사용합니다 (Redis 비활성화)`n" -ForegroundColor Cyan

# 1. 데이터베이스 생성 안내
Write-Host "[1/2] 데이터베이스 준비" -ForegroundColor Yellow
Write-Host "로컬 MariaDB/MySQL에 접속하여 다음 명령어를 실행하세요:`n" -ForegroundColor Cyan

Write-Host "mysql -u root -p" -ForegroundColor White
Write-Host ""
Write-Host 'CREATE DATABASE IF NOT EXISTS planit_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' -ForegroundColor White
Write-Host 'CREATE USER IF NOT EXISTS ''planit_user''@''localhost'' IDENTIFIED BY ''planit_password'';' -ForegroundColor White
Write-Host 'GRANT ALL PRIVILEGES ON planit_user_db.* TO ''planit_user''@''localhost'';' -ForegroundColor White
Write-Host 'FLUSH PRIVILEGES;' -ForegroundColor White
Write-Host 'EXIT;' -ForegroundColor White
Write-Host ""

$dbReady = Read-Host "데이터베이스 준비가 완료되었습니까? (y/n)"
if ($dbReady -ne 'y' -and $dbReady -ne 'Y') {
    Write-Host "데이터베이스를 먼저 준비해주세요." -ForegroundColor Yellow
    exit 0
}

# 2. 환경 변수 설정
Write-Host "`n[2/2] 환경 변수 설정 중..." -ForegroundColor Yellow
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
$env:SPRING_PROFILES_ACTIVE = "local-no-redis"

Write-Host "✓ 환경 변수 설정 완료" -ForegroundColor Green

# 3. Spring Boot 애플리케이션 실행
Write-Host "`n=== Spring Boot 애플리케이션 시작 ===" -ForegroundColor Green
Write-Host "Gradle로 실행 중..." -ForegroundColor Cyan
Write-Host ""

.\gradlew.bat bootRun

Write-Host "`n=== 테스트 명령어 ===" -ForegroundColor Green
Write-Host "헬스체크:" -ForegroundColor Cyan
Write-Host "  curl http://localhost:8080/actuator/health`n" -ForegroundColor White

Write-Host "카테고리 조회:" -ForegroundColor Cyan
Write-Host "  curl http://localhost:8080/api/v1/users/categories`n" -ForegroundColor White

Write-Host "Swagger UI:" -ForegroundColor Cyan
Write-Host "  http://localhost:8080/swagger-ui/index.html`n" -ForegroundColor White

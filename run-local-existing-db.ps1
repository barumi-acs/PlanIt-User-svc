# PlanIt User Service 로컬 실행 스크립트 (기존 MariaDB/MySQL 사용)

Write-Host "=== PlanIt User Service 로컬 실행 (기존 DB 사용) ===" -ForegroundColor Green
Write-Host "로컬에 설치된 MariaDB/MySQL을 사용합니다.`n" -ForegroundColor Cyan

# 1. 데이터베이스 생성 안내
Write-Host "[1/3] 데이터베이스 준비" -ForegroundColor Yellow
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

# 2. Redis만 Docker로 실행
Write-Host "`n[2/3] Redis 시작 중..." -ForegroundColor Yellow
docker run -d --name planit-redis -p 6379:6379 redis:7.2-alpine

if ($LASTEXITCODE -ne 0) {
    Write-Host "Redis 컨테이너가 이미 실행 중일 수 있습니다. 계속 진행합니다..." -ForegroundColor Yellow
}

Write-Host "✓ Redis 준비 완료" -ForegroundColor Green

# 3. 환경 변수 설정
Write-Host "`n[3/3] 환경 변수 설정 중..." -ForegroundColor Yellow
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

Write-Host "✓ 환경 변수 설정 완료" -ForegroundColor Green

# 4. Spring Boot 애플리케이션 실행 안내
Write-Host "`n=== Spring Boot 애플리케이션 시작 ===" -ForegroundColor Green
Write-Host "IntelliJ IDEA에서 실행하거나, 아래 명령어를 실행하세요:`n" -ForegroundColor Cyan
Write-Host "  .\gradlew.bat bootRun" -ForegroundColor White
Write-Host "`n또는 JAR 파일로 실행:" -ForegroundColor Cyan
Write-Host "  .\gradlew.bat bootJar" -ForegroundColor White
Write-Host "  java -jar build\libs\*.jar`n" -ForegroundColor White

Write-Host "=== 테스트 명령어 ===" -ForegroundColor Green
Write-Host "헬스체크:" -ForegroundColor Cyan
Write-Host "  curl http://localhost:8080/actuator/health`n" -ForegroundColor White

Write-Host "카테고리 조회:" -ForegroundColor Cyan
Write-Host "  curl http://localhost:8080/api/v1/users/categories`n" -ForegroundColor White

Write-Host "Swagger UI:" -ForegroundColor Cyan
Write-Host "  http://localhost:8080/swagger-ui/index.html`n" -ForegroundColor White

Write-Host "=== 종료 방법 ===" -ForegroundColor Green
Write-Host "1. Spring Boot 애플리케이션: Ctrl+C" -ForegroundColor Yellow
# && 대신 PowerShell 호환성이 좋은 ; 세미콜론으로 변경했습니다.
Write-Host "2. Redis 컨테이너: docker stop planit-redis ; docker rm planit-redis`n" -ForegroundColor Yellow

# IntelliJ IDEA 실행 여부 확인
$choice = Read-Host "IntelliJ IDEA에서 실행하시겠습니까? (y/n)"
if ($choice -eq 'y' -or $choice -eq 'Y') {
    Write-Host "`nIntelliJ IDEA에서 다음 단계를 진행하세요:" -ForegroundColor Cyan
    Write-Host "1. 프로젝트 열기" -ForegroundColor White
    Write-Host "2. src/main/java/com/planit/basetemplate/PlanitBaseTemplateApplication.java 찾기" -ForegroundColor White
    Write-Host "3. 우클릭 > Run 'PlanitBaseTemplateApplication'" -ForegroundColor White
    Write-Host "`n환경 변수는 이미 설정되었습니다!" -ForegroundColor Green
} else {
    Write-Host "`nGradle로 실행 중..." -ForegroundColor Cyan
    .\gradlew.bat bootRun
}

# PlanIt User Service 로컬 실행 스크립트 (Docker 없이)

Write-Host "=== PlanIt User Service 로컬 실행 ===" -ForegroundColor Green
Write-Host "Docker 없이 로컬 환경에서 직접 실행합니다.`n" -ForegroundColor Cyan

# 1. MariaDB와 Redis만 Docker로 실행
Write-Host "[1/3] MariaDB와 Redis 시작 중..." -ForegroundColor Yellow
docker-compose -f docker-compose.local.yml up -d mariadb redis

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Docker 컨테이너 시작 실패" -ForegroundColor Red
    Write-Host "Docker Desktop이 실행 중인지 확인하세요." -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ MariaDB와 Redis 시작 완료" -ForegroundColor Green

# 2. 데이터베이스 준비 대기
Write-Host "`n[2/3] 데이터베이스 준비 대기 중..." -ForegroundColor Yellow
Start-Sleep -Seconds 10
Write-Host "✓ 데이터베이스 준비 완료" -ForegroundColor Green

# 3. 환경 변수 설정
Write-Host "`n[3/3] 환경 변수 설정 중..." -ForegroundColor Yellow
$env:SPRING_DATASOURCE_URL = "jdbc:mariadb://localhost:13306/planit_user_db"  # 포트 13306 사용
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

# 4. Spring Boot 애플리케이션 실행
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
Write-Host "2. Docker 컨테이너: docker-compose -f docker-compose.local.yml down`n" -ForegroundColor Yellow

# IntelliJ IDEA 실행 여부 확인
$choice = Read-Host "IntelliJ IDEA에서 실행하시겠습니까? (y/n)"
if ($choice -eq 'y' -or $choice -eq 'Y') {
    Write-Host "`nIntelliJ IDEA에서 다음 단계를 진행하세요:" -ForegroundColor Cyan
    Write-Host "1. 프로젝트 열기" -ForegroundColor White
    Write-Host "2. src/main/java/com/planit/userservice/UserServiceApplication.java 찾기" -ForegroundColor White
    Write-Host "3. 우클릭 > Run 'UserServiceApplication'" -ForegroundColor White
    Write-Host "`n환경 변수는 이미 설정되었습니다!" -ForegroundColor Green
} else {
    Write-Host "`nGradle로 실행 중..." -ForegroundColor Cyan
    .\gradlew.bat bootRun
}

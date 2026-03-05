# PlanIt User Service 로컬 빌드 및 실행 스크립트 (Windows PowerShell)

Write-Host "=== PlanIt User Service 로컬 빌드 및 실행 ===" -ForegroundColor Green

# 1. 기존 컨테이너 정리
Write-Host "`n[1/4] 기존 컨테이너 정리 중..." -ForegroundColor Yellow
docker-compose -f docker-compose.local.yml down -v

# 2. Gradle 빌드 (테스트 제외)
Write-Host "`n[2/4] Gradle 빌드 중 (테스트 제외)..." -ForegroundColor Yellow
Write-Host "주의: JAVA_HOME 경로에 공백이 있으면 오류가 발생할 수 있습니다." -ForegroundColor Cyan

# Java 버전 확인
Write-Host "`nJava 버전 확인:" -ForegroundColor Cyan
java -version

# Gradle 빌드 시도
$buildSuccess = $false
try {
    .\gradlew.bat clean bootJar -x test
    if ($LASTEXITCODE -eq 0) {
        $buildSuccess = $true
        Write-Host "✓ Gradle 빌드 성공!" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Gradle 빌드 실패" -ForegroundColor Red
}

if (-not $buildSuccess) {
    Write-Host "`n오류: Gradle 빌드에 실패했습니다." -ForegroundColor Red
    Write-Host "해결 방법:" -ForegroundColor Yellow
    Write-Host "1. JAVA_HOME 환경 변수 확인: $env:JAVA_HOME" -ForegroundColor Cyan
    Write-Host "2. Java 17이 설치되어 있는지 확인" -ForegroundColor Cyan
    Write-Host "3. IntelliJ IDEA에서 빌드 시도" -ForegroundColor Cyan
    exit 1
}

# 3. JAR 파일 확인
Write-Host "`n[3/4] JAR 파일 확인 중..." -ForegroundColor Yellow
$jarFiles = Get-ChildItem -Path "build\libs\*.jar" -ErrorAction SilentlyContinue
if ($jarFiles.Count -eq 0) {
    Write-Host "✗ JAR 파일을 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}
Write-Host "✓ JAR 파일 발견: $($jarFiles[0].Name)" -ForegroundColor Green

# 4. Docker Compose 실행
Write-Host "`n[4/4] Docker Compose 실행 중..." -ForegroundColor Yellow
docker-compose -f docker-compose.local.yml up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ 모든 서비스가 시작되었습니다!" -ForegroundColor Green
    Write-Host "`n서비스 상태 확인:" -ForegroundColor Cyan
    Start-Sleep -Seconds 3
    docker-compose -f docker-compose.local.yml ps
    
    Write-Host "`n로그 확인:" -ForegroundColor Cyan
    Write-Host "docker-compose -f docker-compose.local.yml logs -f user-service" -ForegroundColor White
    
    Write-Host "`n헬스체크:" -ForegroundColor Cyan
    Write-Host "curl http://localhost:8080/actuator/health" -ForegroundColor White
    
    Write-Host "`n종료:" -ForegroundColor Cyan
    Write-Host "docker-compose -f docker-compose.local.yml down" -ForegroundColor White
} else {
    Write-Host "`n✗ Docker Compose 실행 실패" -ForegroundColor Red
    exit 1
}

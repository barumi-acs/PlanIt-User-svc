# PlanIt User Service API 테스트 스크립트

Write-Host "=== PlanIt User Service API 테스트 ===" -ForegroundColor Green
Write-Host ""

# 1. 카테고리 조회
Write-Host "1. 카테고리 목록 조회" -ForegroundColor Cyan
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/users/categories" -Method GET -UseBasicParsing
Write-Host $response.Content
Write-Host ""

# 2. 약관 조회
Write-Host "2. 약관 목록 조회" -ForegroundColor Cyan
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/users/terms" -Method GET -UseBasicParsing
Write-Host $response.Content
Write-Host ""

# 3. Health Check
Write-Host "3. Health Check" -ForegroundColor Cyan
$response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -UseBasicParsing
Write-Host $response.Content
Write-Host ""

Write-Host "=== 테스트 완료 ===" -ForegroundColor Green
Write-Host ""
Write-Host "Swagger UI: http://localhost:8080/swagger-ui/index.html" -ForegroundColor Yellow

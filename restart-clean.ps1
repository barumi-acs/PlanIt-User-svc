# Complete clean restart script

Write-Host "=== PlanIt User Service Complete Restart ===" -ForegroundColor Cyan

# 1. Stop all Java processes
Write-Host "`n1. Stopping Java processes..." -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# 2. Delete build folder
Write-Host "`n2. Deleting build folder..." -ForegroundColor Yellow
if (Test-Path "build") {
    Remove-Item -Path "build" -Recurse -Force
    Write-Host "   build folder deleted" -ForegroundColor Green
}

# 3. Clean Gradle cache
Write-Host "`n3. Cleaning Gradle cache..." -ForegroundColor Yellow
.\gradlew.bat clean

# 4. Rebuild and run
Write-Host "`n4. Rebuilding and running application..." -ForegroundColor Yellow
Write-Host "   (This will take 1-2 minutes)" -ForegroundColor Gray
.\gradlew.bat bootRun --no-daemon

Write-Host "`n=== Execution Complete ===" -ForegroundColor Green
Write-Host "Open browser and go to: http://localhost:8080/api/v1/users/categories" -ForegroundColor Cyan

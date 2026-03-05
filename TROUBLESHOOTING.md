# 로컬 테스트 문제 해결 가이드

## 401 에러 해결 방법

### 방법 1: 확실한 재시작 (권장)

현재 터미널에서 실행 중인 애플리케이션을 `Ctrl+C`로 종료한 후:

```powershell
cd PlanIt-User-svc
.\restart-clean.ps1
```

이 스크립트는:
1. 모든 Java 프로세스 강제 종료
2. 빌드 폴더 완전 삭제
3. Gradle 캐시 정리
4. 애플리케이션 재빌드 및 실행

### 방법 2: Security 의존성 완전 제거

만약 방법 1로도 해결되지 않는다면, Security를 아예 비활성화:

1. `build.gradle` 파일에서 Security 의존성이 이미 주석 처리되어 있는지 확인:
```gradle
// implementation 'org.springframework.boot:spring-boot-starter-security'
// testImplementation 'org.springframework.security:spring-security-test'
```

2. SecurityConfig.java와 JwtAuthenticationFilter.java 파일명 변경:
```powershell
cd src/main/java/com/planit/userservice/config
Rename-Item SecurityConfig.java SecurityConfig.java.bak

cd ../security
Rename-Item JwtAuthenticationFilter.java JwtAuthenticationFilter.java.bak
```

3. 재빌드:
```powershell
cd PlanIt-User-svc
.\gradlew.bat clean bootRun
```

### 테스트 확인

애플리케이션이 실행되면:

1. **브라우저에서 직접 확인** (가장 확실):
   ```
   http://localhost:8080/api/v1/users/categories
   ```
   JSON 응답이 보여야 합니다.

2. **PowerShell 스크립트로 확인**:
   ```powershell
   .\test-api.ps1
   ```

3. **Swagger UI 확인**:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

## 예상 결과

정상적으로 작동하면 다음과 같은 JSON 응답을 받아야 합니다:

```json
{
  "code": 2000,
  "message": "Success",
  "data": [
    {
      "id": "cat-001",
      "name": "운동",
      "description": "건강 관리 및 체력 증진"
    },
    ...
  ],
  "timestamp": "2026-03-04T..."
}
```

## 여전히 401 에러가 발생한다면

1. 애플리케이션 로그 확인:
   - 터미널에서 에러 메시지 확인
   - `logs/application.log` 파일 확인

2. 포트 충돌 확인:
   ```powershell
   netstat -ano | findstr :8080
   ```

3. Java 버전 확인:
   ```powershell
   java -version
   ```
   Java 17 이상이어야 합니다.

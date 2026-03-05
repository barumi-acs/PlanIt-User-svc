# 빠른 빌드 가이드

## 가장 쉬운 방법: IntelliJ IDEA 사용

### 1단계: IntelliJ에서 프로젝트 열기
```
File > Open > PlanIt-User-svc 폴더 선택
```

### 2단계: Gradle 빌드 실행
```
우측 Gradle 패널 열기
Tasks > build > bootJar 더블클릭
```

또는 IntelliJ 하단 터미널에서:
```bash
./gradlew bootJar -x test
```

### 3단계: JAR 파일 확인
```
build/libs/ 폴더에 .jar 파일 생성 확인
```

### 4단계: Docker 실행
```powershell
# PowerShell에서 실행
docker-compose -f docker-compose.local.yml up -d

# 로그 확인
docker-compose -f docker-compose.local.yml logs -f user-service

# 헬스체크
curl http://localhost:8080/actuator/health
```

## 대안: Docker 없이 로컬 실행

### 1단계: 데이터베이스만 Docker로 실행
```powershell
cd PlanIt-User-svc
docker-compose -f docker-compose.local.yml up -d mariadb redis
```

### 2단계: IntelliJ에서 애플리케이션 실행
```
Run > Edit Configurations > Spring Boot
Main class: com.planit.userservice.UserServiceApplication
Environment variables:
  SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/planit_user_db
  SPRING_DATASOURCE_USERNAME=planit_user
  SPRING_DATASOURCE_PASSWORD=planit_password
  SPRING_DATA_REDIS_HOST=localhost
  JWT_SECRET=your-secret-key-change-in-production-must-be-at-least-256-bits-long
```

### 3단계: 실행 및 테스트
```
Run 버튼 클릭 또는 Shift+F10
브라우저에서 http://localhost:8080/actuator/health 접속
```

## 문제 해결

### IntelliJ에서 Java 버전 설정
```
File > Project Structure > Project
SDK: 17 (Adoptium) 선택
Language level: 17
```

### Gradle JVM 설정
```
Settings > Build, Execution, Deployment > Build Tools > Gradle
Gradle JVM: Project SDK (17) 선택
```

## 다음 단계

빌드가 성공하면:
1. `build/libs/` 폴더에 JAR 파일 생성됨
2. Docker Compose로 전체 스택 실행 가능
3. Frontend 연동 테스트 진행

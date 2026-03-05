# PlanIt User Service 빌드 가이드

## 문제 상황

Windows 환경에서 JAVA_HOME 경로에 공백이 포함되어 있어 Gradle 빌드가 실패하는 경우가 있습니다.

```
ERROR: JAVA_HOME is set to an invalid directory: C:\Program Files\Adoptium\jdk-17.0.8.7-hotspot
```

## 해결 방법

### 방법 1: IntelliJ IDEA에서 빌드 (가장 쉬움)

1. IntelliJ IDEA에서 `PlanIt-User-svc` 프로젝트 열기
2. 우측 Gradle 패널 열기
3. `Tasks` > `build` > `bootJar` 더블클릭
4. 또는 터미널에서: `./gradlew bootJar -x test`
5. `build/libs/` 폴더에 JAR 파일 생성 확인

### 방법 2: JAVA_HOME 환경 변수 수정

#### 임시 수정 (현재 PowerShell 세션만)

```powershell
# Java 17 경로 확인
$javaPath = "C:\Program Files\Adoptium\jdk-17.0.8.7-hotspot"

# 경로가 존재하는지 확인
Test-Path $javaPath

# JAVA_HOME 임시 설정 (따옴표로 감싸기)
$env:JAVA_HOME = "`"$javaPath`""

# 또는 짧은 경로 사용 (8.3 형식)
$env:JAVA_HOME = "C:\PROGRA~1\Adoptium\jdk-17.0.8.7-hotspot"

# 빌드 시도
.\gradlew.bat clean bootJar -x test
```

#### 영구 수정 (시스템 환경 변수)

1. Windows 검색에서 "환경 변수" 검색
2. "시스템 환경 변수 편집" 클릭
3. "환경 변수" 버튼 클릭
4. 시스템 변수에서 `JAVA_HOME` 찾기
5. 값을 따옴표로 감싸거나 짧은 경로로 변경:
   - `"C:\Program Files\Adoptium\jdk-17.0.8.7-hotspot"`
   - 또는 `C:\PROGRA~1\Adoptium\jdk-17.0.8.7-hotspot`
6. PowerShell 재시작 후 빌드 시도

### 방법 3: Java 25 사용 (현재 실행 중인 버전)

현재 시스템에 Java 25가 설치되어 있고 실행 가능합니다.

```powershell
# Java 25 경로 찾기
where.exe java

# JAVA_HOME을 Java 25로 변경
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.1"  # 실제 경로로 변경

# 빌드 시도
.\gradlew.bat clean bootJar -x test
```

**주의:** `build.gradle`에서 Java 17을 요구하므로 Java 25로 빌드 시 호환성 문제가 발생할 수 있습니다.

### 방법 4: Gradle Wrapper 직접 실행

```powershell
# Gradle Wrapper를 직접 실행 (JAVA_HOME 무시)
.\gradle\wrapper\gradle-wrapper.jar

# 또는 Java 명령어로 직접 실행
java -jar gradle\wrapper\gradle-wrapper.jar clean bootJar -x test
```

## Docker 실행

빌드가 성공하면 `build/libs/` 폴더에 JAR 파일이 생성됩니다.

```powershell
# JAR 파일 확인
Get-ChildItem build\libs\*.jar

# Docker Compose 실행 (로컬 빌드 JAR 사용)
docker-compose -f docker-compose.local.yml up -d

# 로그 확인
docker-compose -f docker-compose.local.yml logs -f user-service

# 헬스체크
curl http://localhost:8080/actuator/health

# 종료
docker-compose -f docker-compose.local.yml down
```

## 대안: Docker 없이 로컬 실행

Docker 없이 로컬에서 직접 실행할 수도 있습니다.

### 1. MariaDB와 Redis 설치 및 실행

```powershell
# Docker로 MariaDB와 Redis만 실행
docker-compose -f docker-compose.local.yml up -d mariadb redis
```

### 2. 환경 변수 설정

`.env` 파일 생성 또는 PowerShell에서 직접 설정:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mariadb://localhost:3306/planit_user_db"
$env:SPRING_DATASOURCE_USERNAME = "planit_user"
$env:SPRING_DATASOURCE_PASSWORD = "planit_password"
$env:SPRING_DATA_REDIS_HOST = "localhost"
$env:SPRING_DATA_REDIS_PORT = "6379"
$env:JWT_SECRET = "your-secret-key-change-in-production-must-be-at-least-256-bits-long"
```

### 3. 애플리케이션 실행

```powershell
# Gradle로 실행
.\gradlew.bat bootRun

# 또는 JAR 파일로 실행
java -jar build\libs\*.jar
```

## 문제 해결

### "Could not find or load main class"

```powershell
# Gradle 캐시 삭제
.\gradlew.bat clean --refresh-dependencies

# 다시 빌드
.\gradlew.bat bootJar -x test
```

### "Port 8080 is already in use"

```powershell
# 포트 사용 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료 (관리자 권한 필요)
taskkill /PID <PID번호> /F
```

### Protobuf 컴파일 오류

`build.gradle` 파일에서 Protobuf 설정이 OS별 자동 감지로 되어 있는지 확인:

```groovy
protobuf {
    protoc { 
        artifact = "com.google.protobuf:protoc:3.24.0" // OS 자동 감지
    }
    plugins { 
        grpc { 
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0" // OS 자동 감지
        } 
    }
    generateProtoTasks { all()*.plugins { grpc {} } }
}
```

## 추천 방법

1. **IntelliJ IDEA 사용** - 가장 쉽고 안정적
2. **JAVA_HOME 수정** - 한 번만 설정하면 계속 사용 가능
3. **Docker 없이 로컬 실행** - Docker 문제 회피

# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# ✨ [핵심 해결책] 윈도우 줄바꿈(CRLF)을 리눅스(LF)로 강제 변환
RUN apk add --no-cache dos2unix && dos2unix gradlew

# Build application
RUN chmod +x gradlew
# ✨ 에러의 원인이 되던 불필요한 스크립트 제거, 깔끔한 빌드 명령어 사용
RUN ./gradlew clean bootJar -x test

# ==========================================

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose ports
EXPOSE 8080 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", \
  "app.jar"]

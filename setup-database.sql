-- PlanIt User Service 데이터베이스 설정 스크립트

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS planit_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'planit_user'@'localhost' IDENTIFIED BY 'planit_password';
GRANT ALL PRIVILEGES ON planit_user_db.* TO 'planit_user'@'localhost';
FLUSH PRIVILEGES;

-- 데이터베이스 선택
USE planit_user_db;

-- 확인
SELECT 'Database setup completed successfully!' AS status;
SHOW DATABASES LIKE 'planit_user_db';

/** 
 * [planit 글로벌 룰 - 예외 처리 변환]
 * 서비스의 모든 예외는 이 클래스에서 공통 응답 규격으로 전환됩니다.
 * @since 2026-02-23
 */

package com.planit.basetemplate.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 파라미터 검증 실패 예외 처리
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("요청 파라미터 검증 실패", kv("message", errorMessage));
        return ApiResponse.<Void>builder()
                .code(ErrorCode.C4001.getCode())
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ErrorCode로 정의된 예외 처리
    @ExceptionHandler(CustomException.class)
    public ApiResponse<Void> handleCustomException(CustomException e) {
        log.warn("비즈니스 예외 발생", 
                kv("code", e.getErrorCode().getCode()), 
                kv("message", e.getErrorCode().getMessage()));
        return ApiResponse.<Void>builder()
                .code(e.getErrorCode().getCode())
                .message(e.getErrorCode().getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAllException(Exception e) {
        log.error("처리되지 않은 예외 발생", kv("message", e.getMessage()), e);
        return ApiResponse.<Void>builder()
                .code(ErrorCode.C5001.getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
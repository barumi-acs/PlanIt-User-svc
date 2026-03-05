/** 
 * [planit 글로벌 룰 - 예외 처리 리스트]
 * 서비스의 모든 예외는 이 클래스에서 정의됩니다.
 * U, S, AI, IS를 사용하여 원하는 예외를 정의합니다.
 * @since 2026-02-23
 */

package com.planit.basetemplate.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러 (C)
    C4001(4001, "잘못된 요청 파라미터입니다."),
    C4011(4011, "인증 토큰이 만료되었습니다."),
    C4012(4012, "인증되지 않은 사용자입니다."),
    C4031(4031, "접근 권한이 없습니다."),
    C4041(4041, "요청한 리소스를 찾을 수 없습니다."),
    C4051(4051, "허용되지 않은 메서드입니다."),
    C5001(5001, "서버 내부 에러가 발생했습니다."),

    // User Service 에러 (U)
    USER_NOT_FOUND(4041, "존재하지 않는 사용자입니다."),
    USER_NICKNAME_DUPLICATED(4001, "이미 사용 중인 닉네임입니다."),
    USER_EMAIL_DUPLICATED(4005, "이미 사용 중인 이메일입니다."),
    USER_TERMS_NOT_AGREED(4111, "필수 약관 동의가 누락되었습니다."),
    USER_REJOIN_RESTRICTED(4114, "탈퇴 한 후 90일 동안 재가입이 불가능합니다."),
    USER_WITHDRAWN(4115, "탈퇴한 사용자입니다."),
    USER_INVALID_CATEGORY_COUNT(4006, "카테고리는 최소 3개, 최대 4개 선택해야 합니다."),
    USER_CATEGORY_NOT_FOUND(4043, "존재하지 않는 카테고리입니다."),
    USER_INVALID_NICKNAME_FORMAT(4008, "닉네임 형식이 올바르지 않습니다."),
    
    // 친구 관련
    USER_FRIEND_ALREADY_EXISTS(4002, "이미 친구 관계이거나 요청 대기 중입니다."),
    USER_FRIEND_REQUEST_INVALID(4003, "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    USER_FRIEND_REQUEST_ALREADY_EXISTS(4004, "이미 친구 요청이 존재합니다."),
    USER_FRIEND_REQUEST_NOT_FOUND(4042, "존재하지 않는 친구 요청입니다."),
    USER_FRIEND_NOT_FOUND(4044, "존재하지 않는 친구 관계입니다."),
    USER_FRIEND_REQUEST_REJECTED(4007, "거절된 친구 요청입니다."),
    USER_BLOCKED_USER(4009, "차단된 사용자입니다."),

    // Cognito 에러 (U - 인증)
    COGNITO_INVALID_TOKEN(4112, "유효하지 않은 Cognito 토큰입니다."),
    COGNITO_USER_NOT_FOUND(4113, "Cognito에 등록되지 않은 사용자입니다."),
    
    // JWT 토큰 에러 (U - 인증)
    JWT_INVALID_TOKEN(4116, "유효하지 않은 JWT 토큰입니다."),
    JWT_EXPIRED_TOKEN(4117, "만료된 JWT 토큰입니다."),
    JWT_REFRESH_TOKEN_INVALID(4118, "유효하지 않은 Refresh 토큰입니다."),

    // Schedule Service 에러 (S)
    SCHEDULE_NOT_FOUND(4041, "존재하지 않는 일정입니다."),
    SCHEDULE_NO_PERMISSION(4031, "권한이 없습니다.");

    private final Integer code;
    private final String message;
}

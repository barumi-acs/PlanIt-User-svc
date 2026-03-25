package com.planit.userservice.security;

import com.planit.basetemplate.common.CustomException;
import com.planit.basetemplate.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private static final String USER_ID_KEY = "userId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (shouldNotFilter(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = null;
        String token = extractTokenFromRequest(request);
        String headerUserId = request.getHeader("X-User-Id");

        // 1. 먼저 유효한 JWT 토큰이 있는지 확인
        if (token != null) {
            try {
                if (jwtProvider.validateToken(token)) {
                    userId = jwtProvider.getUserIdFromToken(token);
                }
            } catch (CustomException e) {
                String clientIp = getClientIp(request);
                if (e.getErrorCode() == ErrorCode.C4011) {
                    log.info("JWT token expired from IP: {}", clientIp);
                } else if (e.getErrorCode() == ErrorCode.U4015) {
                    log.warn("JWT token invalid or signature mismatch from IP: {}", clientIp);
                }
            } catch (Exception e) {
                log.warn("JWT validation failed from IP: {}", getClientIp(request));
            }
        }

        // 2. 토큰이 없거나 유효하지 않은 경우 X-User-Id 헤더 확인 (개발 환경용)
        if (userId == null && StringUtils.hasText(headerUserId)) {
            log.info("Using X-User-Id header for authentication: {}", headerUserId);
            userId = headerUserId;
        }

        // 3. 인증 정보 설정
        if (userId != null) {
            setAuthentication(userId, request);
            // MDC에 userId 저장 (구조화된 로깅)
            MDC.put(USER_ID_KEY, userId);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private void setAuthentication(String userId, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean shouldNotFilter(String path) {
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api/v1/auth") ||
               path.startsWith("/api/v1/users/auth/login") ||
               path.startsWith("/api/v1/users/auth/signup") ||
               path.startsWith("/api/v1/users/auth/check-withdrawn") ||  // ✅ 추가
               path.startsWith("/api/v1/users/categories") ||
               path.startsWith("/api/v1/users/terms") ||
               path.startsWith("/actuator");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}

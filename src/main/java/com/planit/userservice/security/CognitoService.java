package com.planit.userservice.security;

import com.planit.basetemplate.common.CustomException;
import com.planit.basetemplate.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CognitoService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;
    private final boolean isLocalProfile;

    public CognitoService(
        CognitoIdentityProviderClient cognitoClient,
        @Value("${aws.cognito.user-pool-id}") String userPoolId,
        @Value("${spring.profiles.active:default}") String activeProfile
    ) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
        this.isLocalProfile = "local-no-redis".equals(activeProfile) || "dummy".equals(userPoolId);
    }

    public String validateIdTokenAndGetCognitoSub(String idToken) {
        if (isLocalProfile) {
            log.info("Local profile detected - extracting sub from idToken directly");
            return extractSubFromToken(idToken);
        }

        try {
            GetUserRequest request = GetUserRequest.builder()
                    .accessToken(idToken)
                    .build();
            GetUserResponse response = cognitoClient.getUser(request);
            return response.username();
        } catch (Exception e) {
            log.error("Cognito ID Token validation failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        }
    }

    // ✅ 추가 - idToken에서 email 추출
    public String extractEmailFromToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decodedBytes);

            Matcher matcher = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"").matcher(payload);
            if (matcher.find()) {
                String email = matcher.group(1);
                log.info("Extracted email from token: {}", email);
                return email;
            }

            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        }
    }

    private String extractSubFromToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decodedBytes);
            log.info("Token payload: {}", payload);

            Matcher matcher = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"").matcher(payload);
            if (matcher.find()) {
                String sub = matcher.group(1);
                log.info("Extracted sub from token: {}", sub);
                return sub;
            }

            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract sub from token: {}", e.getMessage());
            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        }
    }

    public void deleteUser(String cognitoSub) {
        if (isLocalProfile) {
            log.info("Local profile detected - skipping Cognito user deletion");
            return;
        }

        try {
            AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoSub)
                    .build();
            cognitoClient.adminDeleteUser(request);
            log.info("Cognito user deleted: {}", cognitoSub);
        } catch (Exception e) {
            log.error("Failed to delete Cognito user: {}", e.getMessage());
        }
    }
}

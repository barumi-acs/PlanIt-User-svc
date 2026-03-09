package com.planit.userservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.planit.basetemplate.common.CustomException;
import com.planit.basetemplate.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;

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

    /**
     * Cognito ID Token을 검증하고 sub claim을 추출합니다.
     * 
     * @param idToken Cognito ID Token (JWT 형식)
     * @return cognito sub (사용자 고유 식별자)
     * @throws CustomException ID Token이 유효하지 않거나 sub claim이 없는 경우
     */
    public String validateIdTokenAndGetCognitoSub(String idToken) {
        try {
            // JWT 디코딩
            DecodedJWT jwt = JWT.decode(idToken);
            
            // token_use 확인 (ID Token인지 검증)
            String tokenUse = jwt.getClaim("token_use").asString();
            if (!"id".equals(tokenUse)) {
                log.error("Invalid token_use: expected 'id', but got '{}'", tokenUse);
                throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
            }
            
            // sub claim 추출
            String sub = jwt.getClaim("sub").asString();
            if (sub == null || sub.isEmpty()) {
                log.error("sub claim is missing in ID Token");
                throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
            }
            
            log.info("Successfully validated ID Token and extracted sub: {}", sub);
            return sub;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cognito ID Token validation failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        }
    }

    /**
     * ID Token에서 email claim을 추출합니다.
     * 
     * @param idToken Cognito ID Token (JWT 형식)
     * @return email 주소
     * @throws CustomException ID Token이 유효하지 않거나 email claim이 없는 경우
     */
    public String extractEmailFromToken(String idToken) {
        try {
            // JWT 디코딩
            DecodedJWT jwt = JWT.decode(idToken);
            
            // email claim 추출
            String email = jwt.getClaim("email").asString();
            if (email == null || email.isEmpty()) {
                log.error("email claim is missing in ID Token");
                throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
            }
            
            log.info("Extracted email from token: {}", email);
            return email;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            throw new CustomException(ErrorCode.COGNITO_INVALID_TOKEN);
        }
    }

    /**
     * Cognito User Pool에서 사용자를 삭제합니다.
     * 
     * @param cognitoSub 삭제할 사용자의 cognito sub
     */
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

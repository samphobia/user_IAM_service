package com.company.iam.service;

import com.company.iam.auth.AaasAuthClient;
import com.company.iam.dto.AuthResponse;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.RefreshTokenRequest;
import com.company.iam.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AaasAuthClient aaasAuthClient;

    public AuthResponse register(RegisterRequest request) {
        Map<String, Object> registerResponse = aaasAuthClient.register(request);

        return AuthResponse.builder()
                .token(null)
                .tokenType("NONE")
                .userId(parseUuid(registerResponse.get("userId")))
                .tenantId(parseUuid(registerResponse.get("tenantId")))
                .externalUserId(parseString(registerResponse.get("externalUserId")))
                .keycloakUserId(parseString(registerResponse.get("keycloakUserId")))
                .status(parseString(registerResponse.get("status")))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Map<String, Object> loginResponse = aaasAuthClient.login(request);
        return AuthResponse.builder()
                .token(parseString(loginResponse.get("accessToken")))
                .refreshToken(parseString(loginResponse.get("refreshToken")))
                .expiresIn(parseLong(loginResponse.get("expiresIn")))
                .tokenType("Bearer")
                .build();
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        Map<String, Object> refreshResponse = aaasAuthClient.refresh(request);
        return AuthResponse.builder()
                .token(parseString(refreshResponse.get("accessToken")))
                .refreshToken(parseString(refreshResponse.get("refreshToken")))
                .expiresIn(parseLong(refreshResponse.get("expiresIn")))
                .tokenType("Bearer")
                .build();
    }

    private UUID parseUuid(Object value) {
        if (!(value instanceof String raw) || raw.isBlank()) {
            return null;
        }
        return UUID.fromString(raw);
    }

    private String parseString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private Long parseLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}

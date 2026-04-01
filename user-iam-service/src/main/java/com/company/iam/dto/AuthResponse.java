package com.company.iam.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AuthResponse {
    String token;
    String refreshToken;
    Long expiresIn;
    String tokenType;
    UUID userId;
    String role;
    UUID merchantId;
    UUID tenantId;
    String externalUserId;
    String keycloakUserId;
    String status;
}

package com.company.iam.auth;

import com.company.iam.config.AaasProperties;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.RegisterRequest;
import com.company.iam.dto.ResetPasswordRequest;
import com.company.iam.dto.VerifyEmailRequest;
import com.company.iam.exception.BadRequestException;
import com.company.iam.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AaasAuthClient {

    private final RestClient restClient;
    private final AaasProperties aaasProperties;

    public record ValidatedPrincipal(String principal, UUID tenantId, Set<String> roles) {
    }

    public Map<String, Object> register(RegisterRequest request) {
        String externalUserId = UUID.nameUUIDFromBytes(request.getEmail().trim().toLowerCase().getBytes()).toString();
        try {
            return restClient.post()
                    .uri(aaasProperties.getBaseUrl() + "/auth/register")
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "email", request.getEmail(),
                            "password", request.getPassword(),
                            "externalUserId", externalUserId,
                            "attributes", Map.of(
                                    "first_name", request.getFirstName(),
                                    "last_name", request.getLastName(),
                                    "phone", request.getPhone() == null ? "" : request.getPhone()
                            )
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 409) {
                throw new BadRequestException("AAAS rejected registration: duplicate identity");
            }
            throw new BadRequestException("AAAS register request failed");
        }
    }

    public Map<String, Object> login(LoginRequest request) {
        try {
            return restClient.post()
                    .uri(aaasProperties.getBaseUrl() + "/auth/login")
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("username", request.getEmail(), "password", request.getPassword()))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new UnauthorizedException("Invalid credentials");
            }
            throw new BadRequestException("AAAS login request failed");
        }
    }

    public void verifyEmail(VerifyEmailRequest request) {
        postWithoutResponse("/verify-email", Map.of("email", request.getEmail(), "otp", request.getOtp()));
    }

    public void forgotPassword(String email) {
        postWithoutResponse("/auth/forgot-password", Map.of("email", email));
    }

    public void resetPassword(ResetPasswordRequest request) {
        postWithoutResponse("/auth/reset-password", Map.of("token", request.getToken(), "newPassword", request.getNewPassword()));
    }

    @SuppressWarnings("unchecked")
    public ValidatedPrincipal validateAccessToken(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(aaasProperties.getBaseUrl() + "/auth/me")
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new UnauthorizedException("Token validation failed");
            }

            String principal = asString(response.get("externalUserId"));
            String tenant = asString(response.get("tenantId"));
            Set<String> roles = new HashSet<>();
            Object rawRoles = response.get("roles");
            if (rawRoles instanceof Collection<?> collection) {
                for (Object role : collection) {
                    if (role != null) {
                        roles.add(role.toString().toUpperCase());
                    }
                }
            }

            if (roles.isEmpty()) {
                roles.add("USER");
            }

            return new ValidatedPrincipal(
                    principal == null || principal.isBlank() ? "anonymous" : principal,
                    tenant == null || tenant.isBlank() ? null : UUID.fromString(tenant),
                    Set.copyOf(roles)
            );
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new UnauthorizedException("Invalid bearer token");
            }
            throw new BadRequestException("AAAS token validation failed");
        }
    }

    private void postWithoutResponse(String path, Map<String, Object> body) {
        try {
            restClient.post()
                    .uri(aaasProperties.getBaseUrl() + path)
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.warn("AAAS request failed path={} status={}", path, ex.getStatusCode());
            throw new BadRequestException("AAAS request failed for " + path);
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}

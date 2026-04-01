package com.company.iam.auth;

import com.company.iam.config.AaasProperties;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.RefreshTokenRequest;
import com.company.iam.dto.RegisterRequest;
import com.company.iam.exception.BadRequestException;
import com.company.iam.exception.UnauthorizedException;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    void checkAaasConnectivity() {
        try {
            restClient.get()
                    .uri(aaasProperties.getBaseUrl() + "/actuator/health")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to connect to AAAS service at APP_AAAS_BASE_URL", ex);
        }
    }

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

    public Map<String, Object> refresh(RefreshTokenRequest request) {
        try {
            return restClient.post()
                    .uri(aaasProperties.getBaseUrl() + "/auth/refresh")
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("refreshToken", request.getRefreshToken()))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            throw new BadRequestException("AAAS refresh request failed");
        }
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

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}

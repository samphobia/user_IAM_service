package com.company.iam.auth;

import com.company.iam.config.AaasProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AaasAuthClient {

    private final RestClient restClient;
    private final AaasProperties aaasProperties;

    public void syncRegister(String email, String password, String externalUserId, Map<String, Object> attributes) {
        if (!aaasProperties.isEnabled()) {
            return;
        }

        try {
            restClient.post()
                    .uri(aaasProperties.getBaseUrl() + "/auth/register")
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "email", email,
                            "password", password,
                            "externalUserId", externalUserId,
                            "attributes", attributes == null ? Map.of() : attributes
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            if (!aaasProperties.isFailOpen()) {
                throw ex;
            }
            log.warn("AAAS register sync failed, continuing in fail-open mode");
        }
    }

    public void syncLogin(String email, String password) {
        if (!aaasProperties.isEnabled()) {
            return;
        }

        try {
            restClient.post()
                    .uri(aaasProperties.getBaseUrl() + "/auth/login")
                    .header("X-API-KEY", aaasProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("username", email, "password", password))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            if (!aaasProperties.isFailOpen()) {
                throw ex;
            }
            log.warn("AAAS login sync failed, continuing in fail-open mode");
        }
    }
}

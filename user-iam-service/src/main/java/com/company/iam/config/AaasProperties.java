package com.company.iam.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.aaas")
public class AaasProperties {

    private String baseUrl;
    private String apiKey;

    @PostConstruct
    void validate() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("APP_AAAS_BASE_URL must be provided");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("APP_AAAS_API_KEY must be provided");
        }
    }
}

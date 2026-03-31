package com.company.iam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.aaas")
public class AaasProperties {

    private boolean enabled;
    private boolean failOpen = true;
    private String baseUrl;
    private String apiKey;
}

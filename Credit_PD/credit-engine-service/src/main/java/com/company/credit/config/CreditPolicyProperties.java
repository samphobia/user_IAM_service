package com.company.credit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "credit")
@Data
public class CreditPolicyProperties {

    private Scoring scoring = new Scoring();
    private Certificate certificate = new Certificate();
    private BankLink bankLink = new BankLink();

    @Data
    public static class Scoring {
        private BigDecimal minimumIncomeThreshold = new BigDecimal("100000");
        private BigDecimal maxDtiPercent = new BigDecimal("40");
        private BigDecimal maxPaydaySweepPercent = new BigDecimal("80");
        private BigDecimal baseLimitMultiplier = new BigDecimal("0.3");
    }

    @Data
    public static class Certificate {
        private long ttlMinutes = 15;
    }

    @Data
    public static class BankLink {
        private long sessionTtlMinutes = 10;
    }
}

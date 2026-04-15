package com.company.credit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonoWebhookRequest {

    @NotBlank
    @JsonProperty("event")
    private String event;

    @NotNull
    private AccountConnectedData data;

    @Data
    public static class AccountConnectedData {
        private String userId;
        private String linkingSessionId;
        @NotBlank
        private String monoAccountId;
        @NotBlank
        private String institutionCode;
        @NotBlank
        private String accountNumberMasked;
        private Integer monthsOfData;
        private BigDecimal averageMonthlyIncome;
        private BigDecimal incomeVolatility;
        private BigDecimal debtToIncomeRatio;
        private BigDecimal lowestMonthlyBalance;
        private String rawData;
    }
}

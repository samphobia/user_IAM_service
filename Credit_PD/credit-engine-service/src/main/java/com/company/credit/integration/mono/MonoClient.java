package com.company.credit.integration.mono;

import com.company.credit.dto.MonoWebhookRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MonoClient {

    public MonoFinancialSnapshot fetchFinancialSnapshot(String monoAccountId, MonoWebhookRequest.AccountConnectedData data) {
        int months = data.getMonthsOfData() == null ? 6 : data.getMonthsOfData();
        return MonoFinancialSnapshot.builder()
                .monthsOfData(months)
                .averageMonthlyIncome(defaultValue(data.getAverageMonthlyIncome(), new BigDecimal("250000")))
                .incomeVolatility(defaultValue(data.getIncomeVolatility(), new BigDecimal("0.12")))
                .debtToIncomeRatio(defaultValue(data.getDebtToIncomeRatio(), new BigDecimal("22")))
                .lowestMonthlyBalance(defaultValue(data.getLowestMonthlyBalance(), new BigDecimal("45000")))
                .paydaySweepRatio(new BigDecimal("0"))
                .rawJson(data.getRawData() == null ? "{\"monoAccountId\":\"" + monoAccountId + "\"}" : data.getRawData())
                .build();
    }

    private BigDecimal defaultValue(BigDecimal value, BigDecimal defaultVal) {
        return value == null ? defaultVal : value;
    }
}

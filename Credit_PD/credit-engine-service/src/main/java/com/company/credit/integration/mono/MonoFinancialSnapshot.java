package com.company.credit.integration.mono;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class MonoFinancialSnapshot {
    int monthsOfData;
    BigDecimal averageMonthlyIncome;
    BigDecimal incomeVolatility;
    BigDecimal debtToIncomeRatio;
    BigDecimal lowestMonthlyBalance;
    BigDecimal paydaySweepRatio;
    String rawJson;
}

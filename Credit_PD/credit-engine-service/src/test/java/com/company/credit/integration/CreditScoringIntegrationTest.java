package com.company.credit.integration;

import com.company.credit.domain.FinancialMetrics;
import com.company.credit.dto.CreditScoreResponse;
import com.company.credit.integration.iam.IamClient;
import com.company.credit.integration.iam.IamUserResponse;
import com.company.credit.repository.FinancialMetricsRepository;
import com.company.credit.service.CreditScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class CreditScoringIntegrationTest {

    @Autowired
    private CreditScoringService creditScoringService;

    @Autowired
    private FinancialMetricsRepository financialMetricsRepository;

    @MockBean
    private IamClient iamClient;

    @BeforeEach
    void setUp() {
        financialMetricsRepository.deleteAll();
        IamUserResponse iamUserResponse = new IamUserResponse();
        iamUserResponse.setId("user-it-1");
        iamUserResponse.setStatus("ACTIVE");
        when(iamClient.getUserById("user-it-1")).thenReturn(iamUserResponse);
        when(iamClient.validateUserActive("user-it-1")).thenReturn(true);

        FinancialMetrics metrics = new FinancialMetrics();
        metrics.setUserId("user-it-1");
        metrics.setAverageMonthlyIncome(new BigDecimal("300000"));
        metrics.setIncomeVolatility(new BigDecimal("0.10"));
        metrics.setDebtToIncomeRatio(new BigDecimal("20"));
        metrics.setLowestMonthlyBalance(new BigDecimal("50000"));
        metrics.setMonthsOfData(6);
        metrics.setPaydaySweepRatio(new BigDecimal("0"));
        financialMetricsRepository.save(metrics);
    }

    @Test
    void shouldScoreApprovedWithPositiveLimit() {
        CreditScoreResponse response = creditScoringService.score("user-it-1");

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getLimit()).isNotNull();
        assertThat(response.getLimit()).isGreaterThan(BigDecimal.ZERO);
    }
}

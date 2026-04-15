package com.company.credit.service;

import com.company.credit.config.CreditPolicyProperties;
import com.company.credit.domain.CreditDecision;
import com.company.credit.domain.CreditDecisionStatus;
import com.company.credit.domain.FinancialMetrics;
import com.company.credit.dto.CreditScoreResponse;
import com.company.credit.mapper.CreditDecisionMapper;
import com.company.credit.repository.CreditDecisionRepository;
import com.company.credit.repository.FinancialMetricsRepository;
import com.company.credit.util.BlacklistServiceStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditScoringServiceTest {

    @Mock
    private IamValidationService iamValidationService;
    @Mock
    private FinancialMetricsRepository financialMetricsRepository;
    @Mock
    private CreditDecisionRepository creditDecisionRepository;
    @Mock
    private CreditDecisionMapper creditDecisionMapper;
    @Mock
    private BlacklistServiceStub blacklistServiceStub;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CreditScoringService creditScoringService;

    @Test
    void shouldRejectWhenDtiIsAboveThreshold() {
        CreditPolicyProperties properties = new CreditPolicyProperties();
        creditScoringService = new CreditScoringService(
                iamValidationService,
                financialMetricsRepository,
                creditDecisionRepository,
                creditDecisionMapper,
                properties,
                blacklistServiceStub,
                objectMapper,
                eventPublisher
        );

        FinancialMetrics metrics = new FinancialMetrics();
        metrics.setAverageMonthlyIncome(new BigDecimal("200000"));
        metrics.setDebtToIncomeRatio(new BigDecimal("45"));
        metrics.setIncomeVolatility(new BigDecimal("0.1"));
        metrics.setPaydaySweepRatio(new BigDecimal("0"));
        metrics.setMonthsOfData(6);

        when(financialMetricsRepository.findTopByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(Optional.of(metrics));
        when(creditDecisionRepository.save(any(CreditDecision.class))).thenAnswer(inv -> inv.getArgument(0));
        when(creditDecisionMapper.toResponse(any(CreditDecision.class))).thenAnswer(inv -> {
            CreditDecision decision = inv.getArgument(0);
            return CreditScoreResponse.builder().status(decision.getStatus().name()).limit(decision.getApprovedLimit()).reason(null).build();
        });

        CreditScoreResponse response = creditScoringService.score("user-1");

        assertThat(response.getStatus()).isEqualTo(CreditDecisionStatus.REJECTED.name());
        assertThat(response.getLimit()).isEqualByComparingTo("0");
        assertThat(response.getReason()).contains("DTI");
    }
}

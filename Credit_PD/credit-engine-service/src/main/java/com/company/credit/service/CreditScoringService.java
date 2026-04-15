package com.company.credit.service;

import com.company.credit.config.CreditPolicyProperties;
import com.company.credit.domain.CreditDecision;
import com.company.credit.domain.CreditDecisionStatus;
import com.company.credit.domain.FinancialMetrics;
import com.company.credit.dto.CreditScoreResponse;
import com.company.credit.events.ScoringCompletedEvent;
import com.company.credit.exception.BadRequestException;
import com.company.credit.mapper.CreditDecisionMapper;
import com.company.credit.repository.CreditDecisionRepository;
import com.company.credit.repository.FinancialMetricsRepository;
import com.company.credit.util.BlacklistServiceStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditScoringService {

    private final IamValidationService iamValidationService;
    private final FinancialMetricsRepository financialMetricsRepository;
    private final CreditDecisionRepository creditDecisionRepository;
    private final CreditDecisionMapper creditDecisionMapper;
    private final CreditPolicyProperties properties;
    private final BlacklistServiceStub blacklistServiceStub;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreditScoreResponse score(String userId) {
        iamValidationService.validateUserActive(userId);

        FinancialMetrics metrics = financialMetricsRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new BadRequestException("No financial metrics available for scoring"));

        CreditDecision decision = new CreditDecision();
        decision.setUserId(userId);
        decision.setStatus(CreditDecisionStatus.SCORING_PENDING);
        decision.setApprovedLimit(BigDecimal.ZERO);
        decision.setDecisionFactors("{}");
        creditDecisionRepository.save(decision);

        Map<String, Object> factors = new LinkedHashMap<>();
        factors.put("averageMonthlyIncome", metrics.getAverageMonthlyIncome());
        factors.put("debtToIncomeRatio", metrics.getDebtToIncomeRatio());
        factors.put("paydaySweepRatio", metrics.getPaydaySweepRatio());
        factors.put("monthsOfData", metrics.getMonthsOfData());

        String reason = null;
        if (blacklistServiceStub.isBlacklisted(userId)) {
            reason = "User is blacklisted";
        } else if (metrics.getAverageMonthlyIncome().compareTo(properties.getScoring().getMinimumIncomeThreshold()) < 0) {
            reason = "income below threshold";
        } else if (metrics.getDebtToIncomeRatio().compareTo(properties.getScoring().getMaxDtiPercent()) > 0) {
            reason = "DTI above 40%";
        } else if (metrics.getPaydaySweepRatio().compareTo(properties.getScoring().getMaxPaydaySweepPercent()) > 0) {
            reason = "payday sweep above 80%";
        }

        if (reason != null) {
            decision.setStatus(CreditDecisionStatus.REJECTED);
            decision.setApprovedLimit(BigDecimal.ZERO);
            factors.put("reason", reason);
        } else {
            BigDecimal base = metrics.getAverageMonthlyIncome().multiply(properties.getScoring().getBaseLimitMultiplier());
            BigDecimal volatilityAdjustment = base.multiply(metrics.getIncomeVolatility());
            BigDecimal dtiAdjustment = base.multiply(metrics.getDebtToIncomeRatio().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            BigDecimal finalLimit = base.subtract(volatilityAdjustment).subtract(dtiAdjustment).max(BigDecimal.ZERO);

            decision.setStatus(CreditDecisionStatus.APPROVED);
            decision.setApprovedLimit(finalLimit.setScale(2, RoundingMode.HALF_UP));
            factors.put("baseLimit", base);
            factors.put("volatilityAdjustment", volatilityAdjustment);
            factors.put("dtiAdjustment", dtiAdjustment);
            factors.put("finalLimit", decision.getApprovedLimit());
            reason = "Approved based on available metrics";
        }

        try {
            decision.setDecisionFactors(objectMapper.writeValueAsString(factors));
        } catch (Exception ex) {
            throw new BadRequestException("Unable to serialize decision factors");
        }

        creditDecisionRepository.save(decision);
        log.info("Scoring decision userId={} status={} limit={} factors={}", userId, decision.getStatus(), decision.getApprovedLimit(), decision.getDecisionFactors());
        eventPublisher.publishEvent(new ScoringCompletedEvent(userId, decision.getStatus()));

        CreditScoreResponse baseResponse = creditDecisionMapper.toResponse(decision);
        return CreditScoreResponse.builder()
                .status(baseResponse.getStatus())
                .limit(baseResponse.getLimit())
                .reason(reason)
                .build();
    }
}

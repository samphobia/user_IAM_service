package com.company.credit.integration;

import com.company.credit.domain.CreditDecision;
import com.company.credit.domain.CreditDecisionStatus;
import com.company.credit.dto.CertificateResponse;
import com.company.credit.exception.ConflictException;
import com.company.credit.integration.iam.IamClient;
import com.company.credit.integration.iam.IamUserResponse;
import com.company.credit.repository.CreditDecisionRepository;
import com.company.credit.service.CreditCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
class CertificateRedemptionIntegrationTest {

    @Autowired
    private CreditCertificateService creditCertificateService;

    @Autowired
    private CreditDecisionRepository creditDecisionRepository;

    @MockBean
    private IamClient iamClient;

    @BeforeEach
    void setUp() {
        creditDecisionRepository.deleteAll();

        IamUserResponse iamUserResponse = new IamUserResponse();
        iamUserResponse.setId("user-cert-1");
        iamUserResponse.setStatus("ACTIVE");
        when(iamClient.getUserById("user-cert-1")).thenReturn(iamUserResponse);
        when(iamClient.validateUserActive("user-cert-1")).thenReturn(true);

        CreditDecision decision = new CreditDecision();
        decision.setUserId("user-cert-1");
        decision.setStatus(CreditDecisionStatus.APPROVED);
        decision.setApprovedLimit(new BigDecimal("120000"));
        decision.setDecisionFactors("{}");
        creditDecisionRepository.save(decision);
    }

    @Test
    void shouldRejectSecondRedemptionWithConflict() {
        CertificateResponse certificate = creditCertificateService.issue("user-cert-1");
        creditCertificateService.redeem("user-cert-1", certificate.getId());

        assertThatThrownBy(() -> creditCertificateService.redeem("user-cert-1", certificate.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already redeemed");
    }
}

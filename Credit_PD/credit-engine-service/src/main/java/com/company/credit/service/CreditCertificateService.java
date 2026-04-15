package com.company.credit.service;

import com.company.credit.config.CreditPolicyProperties;
import com.company.credit.domain.CertificateStatus;
import com.company.credit.domain.CreditCertificate;
import com.company.credit.domain.CreditDecision;
import com.company.credit.domain.CreditDecisionStatus;
import com.company.credit.dto.CertificateResponse;
import com.company.credit.exception.ConflictException;
import com.company.credit.exception.NotFoundException;
import com.company.credit.mapper.CreditCertificateMapper;
import com.company.credit.repository.CreditCertificateRepository;
import com.company.credit.repository.CreditDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCertificateService {

    private final IamValidationService iamValidationService;
    private final CreditDecisionRepository creditDecisionRepository;
    private final CreditCertificateRepository creditCertificateRepository;
    private final CreditCertificateMapper creditCertificateMapper;
    private final CreditPolicyProperties properties;

    @Transactional
    public CertificateResponse issue(String userId) {
        iamValidationService.validateUserActive(userId);
        CreditDecision decision = creditDecisionRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, CreditDecisionStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("No approved credit decision found"));

        CreditCertificate certificate = new CreditCertificate();
        certificate.setUserId(userId);
        certificate.setApprovedAmount(decision.getApprovedLimit());
        certificate.setStatus(CertificateStatus.ISSUED);
        certificate.setExpiresAt(Instant.now().plus(properties.getCertificate().getTtlMinutes(), ChronoUnit.MINUTES));

        creditCertificateRepository.save(certificate);
        log.info("State transition CreditCertificate id={} status={}", certificate.getId(), certificate.getStatus());
        return creditCertificateMapper.toResponse(certificate);
    }

    @Transactional
    public void redeem(String userId, UUID certificateId) {
        iamValidationService.validateUserActive(userId);

        CreditCertificate certificate = creditCertificateRepository.findForUpdateById(certificateId)
                .orElseThrow(() -> new NotFoundException("Certificate not found"));

        if (!certificate.getUserId().equals(userId)) {
            throw new ConflictException("Certificate does not belong to authenticated user");
        }

        if (certificate.getStatus() == CertificateStatus.REDEEMED) {
            throw new ConflictException("Certificate already redeemed");
        }

        if (certificate.getExpiresAt().isBefore(Instant.now())) {
            certificate.setStatus(CertificateStatus.EXPIRED);
            creditCertificateRepository.save(certificate);
            throw new ConflictException("Certificate expired");
        }

        certificate.setStatus(CertificateStatus.REDEEMED);
        creditCertificateRepository.save(certificate);
        log.info("State transition CreditCertificate id={} status={}", certificate.getId(), certificate.getStatus());
    }
}

package com.company.credit.service;

import com.company.credit.config.CreditPolicyProperties;
import com.company.credit.domain.BankAccountStatus;
import com.company.credit.dto.StartBankLinkResponse;
import com.company.credit.exception.ConflictException;
import com.company.credit.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BankLinkService {

    private static final String LINK_SESSION_PREFIX = "credit:bank-link:";

    private final IamValidationService iamValidationService;
    private final BankAccountRepository bankAccountRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final CreditPolicyProperties properties;

    public StartBankLinkResponse startLinking(String userId) {
        iamValidationService.validateUserActive(userId);

        if (bankAccountRepository.existsByUserIdAndStatus(userId, BankAccountStatus.ACTIVE)) {
            throw new ConflictException("User already has an active linked account");
        }

        String sessionId = UUID.randomUUID().toString();
        long ttlMinutes = properties.getBankLink().getSessionTtlMinutes();
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);

        stringRedisTemplate.opsForValue().set(LINK_SESSION_PREFIX + sessionId, userId, ttlMinutes, TimeUnit.MINUTES);

        return StartBankLinkResponse.builder()
                .linkingSessionId(sessionId)
                .expiresAt(expiresAt)
                .build();
    }

    public String resolveSessionUser(String linkingSessionId) {
        return stringRedisTemplate.opsForValue().get(LINK_SESSION_PREFIX + linkingSessionId);
    }
}

package com.company.credit.service;

import com.company.credit.domain.BankAccount;
import com.company.credit.domain.BankAccountStatus;
import com.company.credit.dto.MonoWebhookRequest;
import com.company.credit.events.AccountConnectedEvent;
import com.company.credit.exception.BadRequestException;
import com.company.credit.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonoWebhookService {

    private final BankAccountRepository bankAccountRepository;
    private final BankLinkService bankLinkService;
    private final IamValidationService iamValidationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handleWebhook(MonoWebhookRequest request) {
        if (!"account.connected".equalsIgnoreCase(request.getEvent())) {
            return;
        }

        MonoWebhookRequest.AccountConnectedData data = request.getData();
        if (data == null) {
            throw new BadRequestException("Missing webhook data");
        }

        bankAccountRepository.findByMonoAccountId(data.getMonoAccountId()).ifPresent(existing -> {
            log.info("Idempotent webhook received for monoAccountId={} status={}", existing.getMonoAccountId(), existing.getStatus());
        });
        if (bankAccountRepository.findByMonoAccountId(data.getMonoAccountId()).isPresent()) {
            return;
        }

        String userId = data.getUserId();
        if ((userId == null || userId.isBlank()) && data.getLinkingSessionId() != null) {
            userId = bankLinkService.resolveSessionUser(data.getLinkingSessionId());
        }
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("Unable to resolve user for account.connected event");
        }

        iamValidationService.validateUserActive(userId);

        BankAccount account = new BankAccount();
        account.setUserId(userId);
        account.setMonoAccountId(data.getMonoAccountId());
        account.setInstitutionCode(data.getInstitutionCode());
        account.setAccountNumberMasked(data.getAccountNumberMasked());
        account.setStatus(BankAccountStatus.ACTIVE);
        bankAccountRepository.save(account);

        log.info("State transition BankAccount userId={} status={}", userId, BankAccountStatus.ACTIVE);
        eventPublisher.publishEvent(new AccountConnectedEvent(userId, data.getMonoAccountId(), data));
    }
}

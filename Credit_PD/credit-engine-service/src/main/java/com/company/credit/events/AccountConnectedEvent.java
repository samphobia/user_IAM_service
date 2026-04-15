package com.company.credit.events;

import com.company.credit.dto.MonoWebhookRequest;

public record AccountConnectedEvent(String userId, String monoAccountId, MonoWebhookRequest.AccountConnectedData data) {
}

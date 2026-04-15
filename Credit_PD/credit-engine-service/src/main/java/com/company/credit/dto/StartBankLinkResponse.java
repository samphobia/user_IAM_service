package com.company.credit.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class StartBankLinkResponse {
    String linkingSessionId;
    Instant expiresAt;
}

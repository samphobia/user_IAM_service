package com.company.credit.events;

import com.company.credit.domain.CreditDecisionStatus;

public record ScoringCompletedEvent(String userId, CreditDecisionStatus status) {
}

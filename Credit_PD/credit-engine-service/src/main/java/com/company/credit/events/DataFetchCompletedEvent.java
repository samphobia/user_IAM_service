package com.company.credit.events;

import com.company.credit.domain.DataFetchStatus;

public record DataFetchCompletedEvent(String userId, DataFetchStatus status) {
}

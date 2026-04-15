package com.company.credit.util;

import org.springframework.stereotype.Service;

@Service
public class BlacklistServiceStub {

    public boolean isBlacklisted(String userId) {
        return false;
    }
}

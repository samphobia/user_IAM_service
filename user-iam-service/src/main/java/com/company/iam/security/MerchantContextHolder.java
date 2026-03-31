package com.company.iam.security;

import java.util.UUID;

public final class MerchantContextHolder {

    private static final ThreadLocal<UUID> MERCHANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

    private MerchantContextHolder() {
    }

    public static void set(UUID userId, UUID merchantId) {
        USER_ID.set(userId);
        MERCHANT_ID.set(merchantId);
    }

    public static UUID getUserId() {
        return USER_ID.get();
    }

    public static UUID getMerchantId() {
        return MERCHANT_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
        MERCHANT_ID.remove();
    }
}

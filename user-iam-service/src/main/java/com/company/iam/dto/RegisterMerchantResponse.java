package com.company.iam.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class RegisterMerchantResponse {
    UUID merchantId;
    UUID adminUserId;
    String status;
}

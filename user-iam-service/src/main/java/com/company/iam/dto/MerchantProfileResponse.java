package com.company.iam.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class MerchantProfileResponse {
    UUID merchantId;
    String businessName;
    String businessEmail;
    String businessCategory;
    String status;
    String tier;
}

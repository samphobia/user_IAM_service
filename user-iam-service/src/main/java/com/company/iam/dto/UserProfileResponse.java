package com.company.iam.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class UserProfileResponse {
    UUID id;
    String email;
    String firstName;
    String lastName;
    String phone;
    String role;
    String status;
    UUID merchantId;
}

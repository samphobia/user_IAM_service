package com.company.credit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RedeemCertificateRequest {
    @NotNull
    private UUID certificateId;
}

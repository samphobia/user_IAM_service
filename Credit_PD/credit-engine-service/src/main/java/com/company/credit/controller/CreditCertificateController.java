package com.company.credit.controller;

import com.company.credit.config.OpenApiConfig;
import com.company.credit.dto.CertificateResponse;
import com.company.credit.dto.RedeemCertificateRequest;
import com.company.credit.service.CreditCertificateService;
import com.company.credit.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Tag(name = "Credit Certificates")
public class CreditCertificateController {

    private final CreditCertificateService creditCertificateService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Issue certificate", description = "Issues a single-use credit certificate for an approved user decision")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Certificate issued"),
            @ApiResponse(responseCode = "404", description = "No approved credit decision found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public CertificateResponse issueCertificate() {
        return creditCertificateService.issue(SecurityUtils.currentUserId());
    }

    @PostMapping("/redeem")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Redeem certificate", description = "Redeems a valid certificate exactly once with concurrency protection")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Certificate redeemed"),
            @ApiResponse(responseCode = "409", description = "Already redeemed or expired"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public void redeem(@Valid @RequestBody RedeemCertificateRequest request) {
        creditCertificateService.redeem(SecurityUtils.currentUserId(), request.getCertificateId());
    }
}

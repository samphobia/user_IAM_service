package com.company.iam.controllers;

import com.company.iam.dto.MerchantProfileResponse;
import com.company.iam.dto.RegisterMerchantRequest;
import com.company.iam.dto.RegisterMerchantResponse;
import com.company.iam.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchants")
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    @Operation(summary = "Register merchant", description = "Creates a merchant and an active MERCHANT_ADMIN user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Merchant registered"),
            @ApiResponse(responseCode = "409", description = "Email conflict"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public RegisterMerchantResponse registerMerchant(@Valid @RequestBody RegisterMerchantRequest request) {
        return merchantService.registerMerchant(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN','MERCHANT_MANAGER','MERCHANT_CASHIER')")
    @SecurityRequirement(name = com.company.iam.config.OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Get my merchant", description = "Returns merchant profile for caller tenant context")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Merchant profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public MerchantProfileResponse getMyMerchantProfile() {
        return merchantService.getMyMerchantProfile();
    }

    @GetMapping("/{merchantId}")
    @PreAuthorize("hasRole('OPERATOR_ADMIN')")
    @SecurityRequirement(name = com.company.iam.config.OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Get merchant by ID", description = "Returns a merchant profile by ID for OPERATOR_ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Merchant profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Merchant not found")
    })
    public MerchantProfileResponse getMerchantById(@PathVariable UUID merchantId) {
        return merchantService.getMerchantById(merchantId);
    }
}

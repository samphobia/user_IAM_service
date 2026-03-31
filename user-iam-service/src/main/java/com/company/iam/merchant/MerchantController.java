package com.company.iam.merchant;

import com.company.iam.dto.MerchantProfileResponse;
import com.company.iam.dto.RegisterMerchantRequest;
import com.company.iam.dto.RegisterMerchantResponse;
import com.company.iam.service.MerchantService;
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
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    public RegisterMerchantResponse registerMerchant(@Valid @RequestBody RegisterMerchantRequest request) {
        return merchantService.registerMerchant(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN','MERCHANT_MANAGER','MERCHANT_CASHIER')")
    public MerchantProfileResponse getMyMerchantProfile() {
        return merchantService.getMyMerchantProfile();
    }

    @GetMapping("/{merchantId}")
    @PreAuthorize("hasRole('OPERATOR_ADMIN')")
    public MerchantProfileResponse getMerchantById(@PathVariable UUID merchantId) {
        return merchantService.getMerchantById(merchantId);
    }
}

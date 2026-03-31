package com.company.iam.service;

import com.company.iam.dto.MerchantProfileResponse;
import com.company.iam.dto.RegisterMerchantRequest;
import com.company.iam.dto.RegisterMerchantResponse;
import com.company.iam.exception.ConflictException;
import com.company.iam.exception.NotFoundException;
import com.company.iam.exception.UnauthorizedException;
import com.company.iam.merchant.Merchant;
import com.company.iam.merchant.MerchantStatus;
import com.company.iam.merchant.MerchantTier;
import com.company.iam.repository.MerchantRepository;
import com.company.iam.repository.UserRepository;
import com.company.iam.role.Role;
import com.company.iam.security.MerchantContextHolder;
import com.company.iam.user.User;
import com.company.iam.user.UserStatus;
import com.company.iam.util.PasswordPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterMerchantResponse registerMerchant(RegisterMerchantRequest request) {
        if (merchantRepository.existsByBusinessEmail(request.getBusinessEmail().trim().toLowerCase())) {
            throw new ConflictException("Business email already exists");
        }

        if (userRepository.existsByEmail(request.getAdminEmail().trim().toLowerCase())) {
            throw new ConflictException("Admin email already exists");
        }

        PasswordPolicyValidator.validate(request.getAdminPassword());

        Merchant merchant = Merchant.builder()
                .merchantId(UUID.randomUUID())
                .businessName(request.getBusinessName().trim())
                .businessType(request.getBusinessType())
                .businessCategory(request.getBusinessCategory().trim())
                .businessEmail(request.getBusinessEmail().trim().toLowerCase())
                .businessPhone(request.getBusinessPhone().trim())
                .rcNumber(request.getRcNumber())
                .addressLine1(request.getAddressLine1().trim())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .country(request.getCountry().trim())
                .postalCode(request.getPostalCode().trim())
                .status(MerchantStatus.PENDING_KYC)
                .tier(MerchantTier.TIER_1)
                .build();

        Merchant savedMerchant = merchantRepository.save(merchant);

        User admin = User.builder()
                .id(UUID.randomUUID())
                .email(request.getAdminEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName().trim())
                .lastName(request.getAdminLastName().trim())
                .phone(request.getAdminPhone())
                .role(Role.MERCHANT_ADMIN)
                .merchant(savedMerchant)
                .status(UserStatus.ACTIVE)
                .build();

        User savedAdmin = userRepository.save(admin);

        return RegisterMerchantResponse.builder()
                .merchantId(savedMerchant.getMerchantId())
                .adminUserId(savedAdmin.getId())
                .status(savedMerchant.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public MerchantProfileResponse getMyMerchantProfile() {
        UUID merchantId = MerchantContextHolder.getMerchantId();
        if (merchantId == null) {
            throw new UnauthorizedException("Merchant context is missing");
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));

        return toProfile(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantProfileResponse getMerchantById(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));

        return toProfile(merchant);
    }

    private MerchantProfileResponse toProfile(Merchant merchant) {
        return MerchantProfileResponse.builder()
                .merchantId(merchant.getMerchantId())
                .businessName(merchant.getBusinessName())
                .businessEmail(merchant.getBusinessEmail())
                .businessCategory(merchant.getBusinessCategory())
                .status(merchant.getStatus().name())
                .tier(merchant.getTier().name())
                .build();
    }
}

package com.company.iam.service;

import com.company.iam.dto.UserProfileResponse;
import com.company.iam.exception.NotFoundException;
import com.company.iam.exception.UnauthorizedException;
import com.company.iam.repository.UserRepository;
import com.company.iam.security.MerchantContextHolder;
import com.company.iam.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserWithinMerchant(UUID userId) {
        UUID merchantId = MerchantContextHolder.getMerchantId();
        if (merchantId == null) {
            throw new UnauthorizedException("Merchant context is required");
        }

        User user = userRepository.findByIdAndMerchant_MerchantId(userId, merchantId)
                .orElseThrow(() -> new NotFoundException("User not found in merchant"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .merchantId(user.getMerchant() == null ? null : user.getMerchant().getMerchantId())
                .build();
    }
}

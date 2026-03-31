package com.company.iam.user;

import com.company.iam.dto.UserProfileResponse;
import com.company.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN','MERCHANT_MANAGER','MERCHANT_CASHIER')")
    public UserProfileResponse getUser(@PathVariable UUID userId) {
        return userService.getUserWithinMerchant(userId);
    }
}

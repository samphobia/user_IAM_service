package com.company.iam.controllers;

import com.company.iam.dto.UserProfileResponse;
import com.company.iam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN','MERCHANT_MANAGER','MERCHANT_CASHIER')")
    @SecurityRequirement(name = com.company.iam.config.OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Get user by ID", description = "Returns merchant-scoped user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserProfileResponse getUser(@PathVariable UUID userId) {
        return userService.getUserWithinMerchant(userId);
    }
}

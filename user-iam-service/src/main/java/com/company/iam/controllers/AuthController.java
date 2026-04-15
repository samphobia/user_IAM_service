package com.company.iam.controllers;

import com.company.iam.dto.AuthResponse;
import com.company.iam.dto.ChangePasswordRequest;
import com.company.iam.dto.ForgotPasswordRequest;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.RefreshTokenRequest;
import com.company.iam.dto.RegisterRequest;
import com.company.iam.dto.ResetPasswordRequest;
import com.company.iam.exception.UnauthorizedException;
import com.company.iam.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Delegates user registration to AAAS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration accepted"),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Delegates login to AAAS and returns token payload")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Delegates token refresh to AAAS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh successful"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Forgot password", description = "Triggers identity provider password reset flow for the provided email")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Reset flow requested"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reset password", description = "Completes password reset flow using reset token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired reset token")
    })
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = com.company.iam.config.OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Change password", description = "Changes current authenticated user password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "401", description = "Invalid bearer token or current password")
    })
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                               Principal principal,
                               @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String principalName = resolvePrincipal(principal);
        String accessToken = extractBearerToken(authorizationHeader);
        authService.changePassword(principalName, accessToken, request.getCurrentPassword(), request.getNewPassword());
    }

    private String resolvePrincipal(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Invalid bearer token or current password");
        }
        return principal.getName();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid bearer token or current password");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new UnauthorizedException("Invalid bearer token or current password");
        }
        return token;
    }
}

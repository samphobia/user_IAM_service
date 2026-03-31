package com.company.iam.controllers;

import com.company.iam.dto.AuthResponse;
import com.company.iam.dto.ForgotPasswordRequest;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.MessageResponse;
import com.company.iam.dto.RegisterRequest;
import com.company.iam.dto.ResetPasswordRequest;
import com.company.iam.dto.VerifyEmailRequest;
import com.company.iam.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Registers a new user with PENDING_EMAIL_VERIFICATION status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration accepted"),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verifies email with short-lived OTP token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verification successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return new MessageResponse("Email verification successful");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Issues a password-reset OTP token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset token issued"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return new MessageResponse("Password reset token issued");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets password using a valid reset token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password reset successful");
    }
}

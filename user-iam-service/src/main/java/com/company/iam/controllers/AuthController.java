package com.company.iam.controllers;

import com.company.iam.dto.AuthResponse;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.RefreshTokenRequest;
import com.company.iam.dto.RegisterRequest;
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
}

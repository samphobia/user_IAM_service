package com.company.iam.controllers;

import com.company.iam.dto.MessageResponse;
import com.company.iam.dto.VerifyEmailRequest;
import com.company.iam.service.AuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Hidden
public class VerifyEmailAliasController {

    private final AuthService authService;

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return new MessageResponse("Email verification successful");
    }
}

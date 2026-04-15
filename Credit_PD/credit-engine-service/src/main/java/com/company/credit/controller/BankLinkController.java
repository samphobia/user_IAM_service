package com.company.credit.controller;

import com.company.credit.config.OpenApiConfig;
import com.company.credit.dto.StartBankLinkResponse;
import com.company.credit.service.BankLinkService;
import com.company.credit.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bank")
@RequiredArgsConstructor
@Tag(name = "Bank Linking")
public class BankLinkController {

    private final BankLinkService bankLinkService;

    @PostMapping("/link")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Start account linking", description = "Starts Mono bank account linking session for authenticated active user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Linking session created"),
            @ApiResponse(responseCode = "400", description = "User is not active"),
            @ApiResponse(responseCode = "409", description = "User already has active linked account")
    })
    public StartBankLinkResponse startLinking() {
        return bankLinkService.startLinking(SecurityUtils.currentUserId());
    }
}

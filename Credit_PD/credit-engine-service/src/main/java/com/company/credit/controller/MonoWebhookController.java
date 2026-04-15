package com.company.credit.controller;

import com.company.credit.dto.MonoWebhookRequest;
import com.company.credit.service.MonoWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Mono Webhooks")
public class MonoWebhookController {

    private final MonoWebhookService monoWebhookService;

    @PostMapping("/mono")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Mono webhook", description = "Handles Mono account webhook events; supports idempotent account.connected processing")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
    })
    public void monoWebhook(@Valid @RequestBody MonoWebhookRequest request) {
        monoWebhookService.handleWebhook(request);
    }
}

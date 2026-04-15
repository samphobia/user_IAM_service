package com.company.credit.controller;

import com.company.credit.config.OpenApiConfig;
import com.company.credit.dto.CreditScoreResponse;
import com.company.credit.service.CreditScoringService;
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
@RequestMapping("/api/v1/credit")
@RequiredArgsConstructor
@Tag(name = "Credit Scoring")
public class CreditScoringController {

    private final CreditScoringService creditScoringService;

    @PostMapping("/score")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Score credit", description = "Runs phase-1 individual credit scoring using financial metrics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scoring completed"),
            @ApiResponse(responseCode = "400", description = "Invalid scoring preconditions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public CreditScoreResponse score() {
        return creditScoringService.score(SecurityUtils.currentUserId());
    }
}

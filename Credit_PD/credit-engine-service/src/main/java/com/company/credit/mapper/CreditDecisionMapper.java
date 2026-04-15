package com.company.credit.mapper;

import com.company.credit.domain.CreditDecision;
import com.company.credit.dto.CreditScoreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditDecisionMapper {

    @Mapping(target = "status", expression = "java(decision.getStatus().name())")
    @Mapping(target = "limit", source = "approvedLimit")
    @Mapping(target = "reason", ignore = true)
    CreditScoreResponse toResponse(CreditDecision decision);
}

package com.company.credit.mapper;

import com.company.credit.domain.CreditCertificate;
import com.company.credit.dto.CertificateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditCertificateMapper {

    @Mapping(target = "status", expression = "java(certificate.getStatus().name())")
    CertificateResponse toResponse(CreditCertificate certificate);
}

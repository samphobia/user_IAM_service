package com.company.credit.service;

import com.company.credit.exception.BadRequestException;
import com.company.credit.integration.iam.IamClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IamValidationService {

    private final IamClient iamClient;

    public void validateUserActive(String userId) {
        if (!iamClient.validateUserActive(userId)) {
            throw new BadRequestException("Complete registration before proceeding");
        }
    }
}

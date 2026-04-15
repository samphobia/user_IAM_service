package com.company.credit.integration.iam;

import com.company.credit.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "iamClient", url = "${integration.iam.base-url}", configuration = FeignConfig.class)
public interface IamClient {

    @GetMapping("/api/v1/users/{userId}")
    IamUserResponse getUserById(@PathVariable("userId") String userId);

    default boolean validateUserActive(String userId) {
        IamUserResponse user = getUserById(userId);
        return user != null && "ACTIVE".equalsIgnoreCase(user.getStatus());
    }
}

package com.company.credit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CreditEngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditEngineServiceApplication.class, args);
    }
}

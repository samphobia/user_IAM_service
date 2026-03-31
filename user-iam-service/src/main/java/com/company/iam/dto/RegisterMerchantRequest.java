package com.company.iam.dto;

import com.company.iam.entities.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterMerchantRequest {

    @NotBlank
    private String businessName;

    @NotNull
    private BusinessType businessType;

    @NotBlank
    private String businessCategory;

    @Email
    @NotBlank
    private String businessEmail;

    @NotBlank
    private String businessPhone;

    private String rcNumber;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @NotBlank
    private String postalCode;

    @Email
    @NotBlank
    private String adminEmail;

    @NotBlank
    private String adminPassword;

    @NotBlank
    private String adminFirstName;

    @NotBlank
    private String adminLastName;

    private String adminPhone;
}

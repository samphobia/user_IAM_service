package com.company.iam.util;

import com.company.iam.exception.BadRequestException;

import java.util.regex.Pattern;

public final class PasswordPolicyValidator {

    private static final Pattern HAS_NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL = Pattern.compile(".*[^a-zA-Z0-9].*");

    private PasswordPolicyValidator() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!HAS_NUMBER.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one number");
        }
        if (!HAS_SPECIAL.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one special character");
        }
    }
}

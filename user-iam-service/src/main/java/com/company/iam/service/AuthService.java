package com.company.iam.service;

import com.company.iam.auth.AaasAuthClient;
import com.company.iam.entities.AuthToken;
import com.company.iam.entities.enums.AuthTokenType;
import com.company.iam.dto.AuthResponse;
import com.company.iam.dto.ForgotPasswordRequest;
import com.company.iam.dto.LoginRequest;
import com.company.iam.dto.RegisterRequest;
import com.company.iam.dto.ResetPasswordRequest;
import com.company.iam.dto.VerifyEmailRequest;
import com.company.iam.exception.BadRequestException;
import com.company.iam.exception.ConflictException;
import com.company.iam.exception.NotFoundException;
import com.company.iam.exception.UnauthorizedException;
import com.company.iam.entities.Merchant;
import com.company.iam.repository.AuthTokenRepository;
import com.company.iam.repository.MerchantRepository;
import com.company.iam.repository.UserRepository;
import com.company.iam.entities.User;
import com.company.iam.entities.enums.UserStatus;
import com.company.iam.util.HashingUtil;
import com.company.iam.util.PasswordPolicyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AaasAuthClient aaasAuthClient;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        PasswordPolicyValidator.validate(request.getPassword());

        Merchant merchant = null;
        if (request.getMerchantId() != null) {
            merchant = merchantRepository.findById(request.getMerchantId())
                    .orElseThrow(() -> new NotFoundException("Merchant not found"));
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .role(request.getRole())
                .merchant(merchant)
                .status(UserStatus.PENDING_EMAIL_VERIFICATION)
                .build();

        User saved = userRepository.save(user);
        String otp = issueOtp(saved, AuthTokenType.EMAIL_VERIFICATION);

        aaasAuthClient.syncRegister(
                saved.getEmail(),
                request.getPassword(),
                saved.getId().toString(),
                Map.of(
                        "first_name", saved.getFirstName(),
                        "last_name", saved.getLastName(),
                        "phone", saved.getPhone() == null ? "" : saved.getPhone()
                )
        );

        log.info("Email verification OTP issued for userId={} (OTP not logged)", saved.getId());

        return AuthResponse.builder()
                .token(null)
                .tokenType("NONE")
                .userId(saved.getId())
                .role(saved.getRole().name())
                .merchantId(saved.getMerchant() == null ? null : saved.getMerchant().getMerchantId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User is not active");
        }

        aaasAuthClient.syncLogin(user.getEmail(), request.getPassword());

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .role(user.getRole().name())
                .merchantId(user.getMerchant() == null ? null : user.getMerchant().getMerchantId())
                .build();
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        AuthToken token = authTokenRepository
                .findFirstByUser_IdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), AuthTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("Verification token not found"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        String requestHash = HashingUtil.sha256(request.getOtp());
        if (!requestHash.equals(token.getTokenHash())) {
            throw new BadRequestException("Invalid verification token");
        }

        token.setConsumedAt(Instant.now());
        user.setStatus(UserStatus.ACTIVE);
        authTokenRepository.save(token);
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        issueOtp(user, AuthTokenType.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordPolicyValidator.validate(request.getNewPassword());

        String hash = HashingUtil.sha256(request.getToken());
        AuthToken token = authTokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(hash, AuthTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        token.setConsumedAt(Instant.now());

        userRepository.save(user);
        authTokenRepository.save(token);
    }

    private String issueOtp(User user, AuthTokenType type) {
        String otp = generateOtp();
        AuthToken token = AuthToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(HashingUtil.sha256(otp))
                .type(type)
                .expiresAt(Instant.now().plus(1, ChronoUnit.MINUTES))
                .build();

        authTokenRepository.save(token);
        return otp;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int value = 100000 + random.nextInt(900000);
        return Integer.toString(value);
    }
}

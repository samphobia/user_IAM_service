package com.company.iam.repository;

import com.company.iam.auth.AuthToken;
import com.company.iam.auth.AuthTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    Optional<AuthToken> findFirstByUser_IdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(UUID userId, AuthTokenType type);
    Optional<AuthToken> findByTokenHashAndTypeAndConsumedAtIsNull(String tokenHash, AuthTokenType type);
}

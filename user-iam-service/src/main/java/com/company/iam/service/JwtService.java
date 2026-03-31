package com.company.iam.service;

import com.company.iam.config.JwtProperties;
import com.company.iam.role.Role;
import com.company.iam.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getExpirationMs());
        UUID merchantId = user.getMerchant() == null ? null : user.getMerchant().getMerchantId();

        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "merchant_id", merchantId == null ? "" : merchantId.toString(),
                "email", user.getEmail()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.get("email", String.class);
        if (email != null && !email.isBlank()) {
            return email;
        }
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.after(new Date());
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public Role extractRole(String token) {
        return Role.valueOf(extractAllClaims(token).get("role", String.class));
    }

    public UUID extractMerchantId(String token) {
        String raw = extractAllClaims(token).get("merchant_id", String.class);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return UUID.fromString(raw);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }
}

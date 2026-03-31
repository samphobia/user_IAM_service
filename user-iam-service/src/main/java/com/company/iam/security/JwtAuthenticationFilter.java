package com.company.iam.security;

import com.company.iam.repository.UserRepository;
import com.company.iam.entities.enums.Role;
import com.company.iam.service.JwtService;
import com.company.iam.entities.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION);

        try {
            if (header != null && header.startsWith(BEARER)) {
                String token = header.substring(BEARER.length());
                if (jwtService.validateToken(token)) {
                    UUID userId = jwtService.extractUserId(token);
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        Role role = jwtService.extractRole(token);
                        UUID merchantId = jwtService.extractMerchantId(token);

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userId.toString(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        MerchantContextHolder.set(userId, merchantId);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            MerchantContextHolder.clear();
        }
    }
}

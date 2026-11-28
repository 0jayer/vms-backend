package com.stark.vms_backend.service;

import com.stark.vms_backend.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * SRP: sole responsibility is JWT creation.
 * OCP: implement a different TokenService to swap the token format without modifying callers.
 */
@Service
public class JwtTokenService implements TokenService {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(expirationMinutes)))
                .build();
        return jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();
    }

    @Override
    public String getTokenType() {
        return TOKEN_TYPE;
    }

    @Override
    public long getExpiresIn() {
        return Duration.ofMinutes(expirationMinutes).toSeconds();
    }
}


package com.stark.vms_backend.service;

import com.stark.vms_backend.entity.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(jwtEncoder, 60L);
    }

    @Test
    void generateToken_returnsEncodedTokenValue() {
        AppUser user = new AppUser("admin", "hashed-password");
        Jwt jwt = Jwt.withTokenValue("signed-jwt")
                .header("alg", "HS256")
                .claim("sub", "admin")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = jwtTokenService.generateToken(user);

        assertEquals("signed-jwt", token);
    }

    @Test
    void generateToken_setsSubjectToUsername() {
        AppUser user = new AppUser("admin", "hashed-password");
        Jwt jwt = Jwt.withTokenValue("signed-jwt")
                .header("alg", "HS256")
                .claim("sub", "admin")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        jwtTokenService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        assertEquals("admin", captor.getValue().getClaims().getSubject());
    }

    @Test
    void getTokenType_returnsBearer() {
        assertEquals("Bearer", jwtTokenService.getTokenType());
    }

    @Test
    void getExpiresIn_returnsExpirationInSeconds() {
        // 60 minutes → 3600 seconds
        assertEquals(3600L, jwtTokenService.getExpiresIn());
    }
}


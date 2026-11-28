package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.LoginRequest;
import com.stark.vms_backend.dto.LoginResponse;
import com.stark.vms_backend.entity.AppUser;
import com.stark.vms_backend.exception.InvalidCredentialsException;
import com.stark.vms_backend.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(users, passwordEncoder, tokenService);
    }

    @Test
    void login_validCredentials_returnsTokenFromTokenService() {
        AppUser user = new AppUser("admin", "hashed-password");
        when(users.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed-password")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("generated-token");
        when(tokenService.getTokenType()).thenReturn("Bearer");
        when(tokenService.getExpiresIn()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("admin", "secret"));

        assertNotNull(response);
        assertEquals("generated-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        verify(tokenService).generateToken(user);
    }

    @Test
    void login_trimsUsernameBeforeLookup() {
        AppUser user = new AppUser("admin", "hashed-password");
        when(users.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed-password")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("generated-token");
        when(tokenService.getTokenType()).thenReturn("Bearer");
        when(tokenService.getExpiresIn()).thenReturn(3600L);

        authService.login(new LoginRequest("  admin  ", "secret"));

        verify(users).findByUsername("admin");
    }

    @Test
    void login_unknownUser_throwsInvalidCredentialsException() {
        when(users.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("unknown", "secret")));
        verify(tokenService, never()).generateToken(null);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        AppUser user = new AppUser("admin", "hashed-password");
        when(users.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("admin", "wrong")));
        verify(tokenService, never()).generateToken(null);
    }
}

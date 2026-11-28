package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.LoginRequest;
import com.stark.vms_backend.dto.LoginResponse;
import com.stark.vms_backend.entity.AppUser;
import com.stark.vms_backend.exception.InvalidCredentialsException;
import com.stark.vms_backend.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * SRP: sole responsibility is credential verification; token creation is delegated to TokenService.
 * OCP: depends on TokenService abstraction — swap token strategies without touching this class.
 * DIP: depends on interfaces (AuthService, TokenService, AppUserRepository, PasswordEncoder),
 *      not concrete implementations.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(AppUserRepository users,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByUsername(request.username().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        String token = tokenService.generateToken(user);
        return new LoginResponse(token, tokenService.getTokenType(), tokenService.getExpiresIn());
    }
}

package com.stark.vms_backend.controller;

import com.stark.vms_backend.config.SecurityConfig;
import com.stark.vms_backend.dto.LoginRequest;
import com.stark.vms_backend.dto.LoginResponse;
import com.stark.vms_backend.exception.GlobalExceptionHandler;
import com.stark.vms_backend.exception.InvalidCredentialsException;
import com.stark.vms_backend.service.AuthService;
import com.stark.vms_backend.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, VehicleController.class})
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@TestPropertySource(properties = "app.jwt.secret=test-only-secret-key-at-least-32-chars-long")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private VehicleService vehicleService;

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("token-value", "Bearer", 3600));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("token-value")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(3600)));
    }

    @Test
    void login_wrongCredentials_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }

    @Test
    void login_emptyBody_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.password", notNullValue()));
    }

    @Test
    void vehicles_withoutToken_returns401Json() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    void vehicles_withInvalidToken_returns401Json() throws Exception {
        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}
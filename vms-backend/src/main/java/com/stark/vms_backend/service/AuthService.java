package com.stark.vms_backend.service;

import com.stark.vms_backend.dto.LoginRequest;
import com.stark.vms_backend.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}

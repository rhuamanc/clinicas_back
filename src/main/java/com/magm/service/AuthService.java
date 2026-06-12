package com.magm.service;

import com.magm.dto.LoginRequest;
import com.magm.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}

package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.AuthResponse;
import com.superdupermart.shopping.dto.LoginRequest;
import com.superdupermart.shopping.dto.RegistrationRequest;

public interface AuthService {
    void register(RegistrationRequest request);
    AuthResponse login(LoginRequest request);
}

package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.AuthResponse;
import com.superdupermart.shopping.dto.LoginRequest;
import com.superdupermart.shopping.dto.RegistrationRequest;
import com.superdupermart.shopping.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/version")
    public ResponseEntity<String> version() {
        return ResponseEntity.ok("v35-security-update");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

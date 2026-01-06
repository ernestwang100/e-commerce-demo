package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.dto.AuthResponse;
import com.superdupermart.shopping.dto.LoginRequest;
import com.superdupermart.shopping.dto.RegistrationRequest;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.exception.InvalidCredentialsException;
import com.superdupermart.shopping.security.JwtProvider;
import com.superdupermart.shopping.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        if (userDao.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userDao.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userDao.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userDao.findByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtProvider.createToken(user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}

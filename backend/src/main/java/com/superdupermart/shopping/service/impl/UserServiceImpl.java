package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.dto.ProfileUpdateRequest;
import com.superdupermart.shopping.dto.UserProfileResponse;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserProfileResponse getProfile(String username) {
        User user = getUserByUsername(username);
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isAdmin(user.getIsAdmin())
                .hasProfilePicture(user.getProfilePicture() != null && user.getProfilePicture().length > 0)
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(String username, ProfileUpdateRequest request) {
        User user = getUserByUsername(username);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check if email is taken by another user
            userDao.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new RuntimeException("Email already in use");
                        }
                    });
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userDao.save(user);
    }

    @Override
    @Transactional
    public void uploadProfilePicture(String username, MultipartFile file) {
        User user = getUserByUsername(username);
        try {
            user.setProfilePicture(file.getBytes());
            user.setProfilePictureContentType(file.getContentType());
            userDao.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    @Override
    public User getProfilePicture(String username) {
        return getUserByUsername(username);
    }

    private User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

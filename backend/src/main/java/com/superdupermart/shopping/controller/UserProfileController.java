package com.superdupermart.shopping.controller;

import com.superdupermart.shopping.dto.ProfileUpdateRequest;
import com.superdupermart.shopping.dto.UserProfileResponse;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
public class UserProfileController {

    private final UserService userService;

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping("/picture")
    public ResponseEntity<String> uploadProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        userService.uploadProfilePicture(userDetails.getUsername(), file);
        return ResponseEntity.ok("Profile picture uploaded successfully");
    }

    @GetMapping("/picture")
    public ResponseEntity<byte[]> getProfilePicture(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getProfilePicture(userDetails.getUsername());
        if (user.getProfilePicture() == null || user.getProfilePicture().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(user.getProfilePictureContentType()))
                .body(user.getProfilePicture());
    }

    @GetMapping("/addresses")
    public ResponseEntity<java.util.List<com.superdupermart.shopping.entity.Address>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getAddresses(userDetails.getUsername()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<Void> addAddress(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody com.superdupermart.shopping.entity.Address address) {
        userService.addAddress(userDetails.getUsername(), address);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<java.util.List<com.superdupermart.shopping.entity.PaymentMethod>> getPaymentMethods(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getPaymentMethods(userDetails.getUsername()));
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<Void> addPaymentMethod(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody com.superdupermart.shopping.entity.PaymentMethod paymentMethod) {
        userService.addPaymentMethod(userDetails.getUsername(), paymentMethod);
        return ResponseEntity.ok().build();
    }
}

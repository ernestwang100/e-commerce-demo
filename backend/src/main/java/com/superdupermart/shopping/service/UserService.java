package com.superdupermart.shopping.service;

import com.superdupermart.shopping.dto.ProfileUpdateRequest;
import com.superdupermart.shopping.dto.UserProfileResponse;
import com.superdupermart.shopping.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getProfile(String username);

    void updateProfile(String username, ProfileUpdateRequest request);

    void uploadProfilePicture(String username, MultipartFile file);

    User getProfilePicture(String username);

    java.util.List<com.superdupermart.shopping.entity.Address> getAddresses(String username);

    void addAddress(String username, com.superdupermart.shopping.entity.Address address);

    java.util.List<com.superdupermart.shopping.entity.PaymentMethod> getPaymentMethods(String username);

    void addPaymentMethod(String username, com.superdupermart.shopping.entity.PaymentMethod paymentMethod);
}

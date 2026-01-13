package com.superdupermart.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Integer id;
    private String username;
    private String email;
    private String role;
    private boolean isAdmin;
    private boolean hasProfilePicture;
}

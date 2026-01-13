package com.superdupermart.shopping.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String password;
}

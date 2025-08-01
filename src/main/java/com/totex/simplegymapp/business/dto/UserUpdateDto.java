package com.totex.simplegymapp.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
    private String currentPassword;

    @Email(message = "Email should be valid")
    private String email;
}

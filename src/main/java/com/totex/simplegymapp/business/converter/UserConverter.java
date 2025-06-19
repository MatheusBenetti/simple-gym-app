package com.totex.simplegymapp.business.converter;

import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.UserUpdateDto;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {
    private final PasswordEncoder passwordEncoder;

    public UserConverter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel toUserModel(UserCreateDto userCreateDto) {
        UserModel user = new UserModel();
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        return user;
    }

    public UserCreateDto toUserDto(UserModel userModel) {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setUsername(userModel.getUsername());
        userDto.setEmail(userDto.getEmail());
        return userDto;
    }

    public void updateUserModel(UserModel user, UserUpdateDto dto) {
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword())); // HASH
        }
    }

    public void updatePassword(UserModel user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}

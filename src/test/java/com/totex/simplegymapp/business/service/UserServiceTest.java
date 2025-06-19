package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.UserConverter;
import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.UserPasswordUpdateDto;
import com.totex.simplegymapp.business.dto.UserResponseDto;
import com.totex.simplegymapp.business.dto.UserUpdateDto;
import com.totex.simplegymapp.infrastructure.exception.ConflictException;
import com.totex.simplegymapp.infrastructure.exception.ResourceNotFoundException;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.repository.UserRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserService userService;

    private UserCreateDto userCreateDto;
    private UserModel userModel;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("testuser");
        userCreateDto.setEmail("test@example.com");
        userCreateDto.setPassword("password123");

        userModel = UserModel.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        userResponseDto = new UserResponseDto();
        userResponseDto.setUserId(1L);
        userResponseDto.setUsername("testuser");
        userResponseDto.setEmail("test@example.com");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userConverter.toUserModel(userCreateDto)).willReturn(userModel);
        given(userRepository.save(userModel)).willReturn(userModel);
        given(userConverter.toUserResponseDto(userModel)).willReturn(userResponseDto);

        // When
        UserResponseDto result = userService.createUser(userCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(userModel);
    }

    @Test
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        // Given
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(userCreateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already exists.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFindUserByEmailSuccessfully() {
        // Given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(userModel));
        given(userConverter.toUserResponseDto(userModel)).willReturn(userResponseDto);

        // When
        UserResponseDto result = userService.findUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        // Given
        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserByEmail("notfound@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Email not found.");
    }

    @Test
    void shouldUpdateUserPasswordSuccessfully() {
        // Given
        String token = "Bearer validtoken";
        UserPasswordUpdateDto dto = new UserPasswordUpdateDto();
        dto.setOldPassword("oldpassword");
        dto.setNewPassword("newpassword");

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(userModel));
        given(passwordEncoder.matches("oldpassword", userModel.getPassword())).willReturn(true);

        // When
        userService.updateUserPassword(token, dto);

        // Then
        verify(userConverter).updatePassword(userModel, "newpassword");
        verify(userRepository).save(userModel);
    }

    @Test
    void shouldThrowConflictExceptionWhenCurrentPasswordIsIncorrect() {
        // Given
        String token = "Bearer validtoken";
        UserPasswordUpdateDto dto = new UserPasswordUpdateDto();
        dto.setOldPassword("wrongpassword");
        dto.setNewPassword("newpassword");

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(userModel));
        given(passwordEncoder.matches("wrongpassword", userModel.getPassword())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.updateUserPassword(token, dto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Current password is incorrect.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserByEmailSuccessfully() {
        // When
        userService.deleteUserByEmail("test@example.com");

        // Then
        verify(userRepository).deleteByEmail("test@example.com");
        verify(redisTemplate).delete("jwt-token:test@example.com");
    }
}
package com.totex.simplegymapp.business.converter;

import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.UserResponseDto;
import com.totex.simplegymapp.business.dto.UserUpdateDto;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserConverterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserConverter userConverter;

    private UserCreateDto userCreateDto;
    private UserModel userModel;

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
    }

    @Test
    void shouldConvertUserCreateDtoToUserModel() {
        // Given
        given(passwordEncoder.encode("password123")).willReturn("hashedpassword");

        // When
        UserModel result = userConverter.toUserModel(userCreateDto);

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("hashedpassword");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldConvertUserModelToUserResponseDto() {
        // When
        UserResponseDto result = userConverter.toUserResponseDto(userModel);

        // Then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldUpdateUserModelWithValidData() {
        // Given
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        updateDto.setNewPassword("newpassword");

        given(passwordEncoder.encode("newpassword")).willReturn("hashednewpassword");

        // When
        userConverter.updateUserModel(userModel, updateDto);

        // Then
        assertThat(userModel.getUsername()).isEqualTo("updateduser");
        assertThat(userModel.getEmail()).isEqualTo("updated@example.com");
        assertThat(userModel.getPassword()).isEqualTo("hashednewpassword");
    }

    @Test
    void shouldNotUpdateUserModelWithBlankValues() {
        // Given
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("");
        updateDto.setEmail("   ");
        updateDto.setNewPassword("");

        String originalUsername = userModel.getUsername();
        String originalEmail = userModel.getEmail();
        String originalPassword = userModel.getPassword();

        // When
        userConverter.updateUserModel(userModel, updateDto);

        // Then
        assertThat(userModel.getUsername()).isEqualTo(originalUsername);
        assertThat(userModel.getEmail()).isEqualTo(originalEmail);
        assertThat(userModel.getPassword()).isEqualTo(originalPassword);
    }

    @Test
    void shouldNotUpdateUserModelWithNullValues() {
        // Given
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername(null);
        updateDto.setEmail(null);
        updateDto.setNewPassword(null);

        String originalUsername = userModel.getUsername();
        String originalEmail = userModel.getEmail();
        String originalPassword = userModel.getPassword();

        // When
        userConverter.updateUserModel(userModel, updateDto);

        // Then
        assertThat(userModel.getUsername()).isEqualTo(originalUsername);
        assertThat(userModel.getEmail()).isEqualTo(originalEmail);
        assertThat(userModel.getPassword()).isEqualTo(originalPassword);
    }

    @Test
    void shouldUpdatePassword() {
        // Given
        String newPassword = "newpassword123";
        given(passwordEncoder.encode(newPassword)).willReturn("hashednewpassword123");

        // When
        userConverter.updatePassword(userModel, newPassword);

        // Then
        assertThat(userModel.getPassword()).isEqualTo("hashednewpassword123");
        verify(passwordEncoder).encode(newPassword);
    }
}
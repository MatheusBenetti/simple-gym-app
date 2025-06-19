package com.totex.simplegymapp.controller;

import com.totex.simplegymapp.base.BaseIntegrationTest;
import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.UserPasswordUpdateDto;
import com.totex.simplegymapp.business.dto.UserUpdateDto;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class UserControllerTest extends BaseIntegrationTest {

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("testuser");
        userCreateDto.setEmail("test@example.com");
        userCreateDto.setPassword("Password@123");

        // When & Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // Given
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("testuser");
        userCreateDto.setEmail("invalid-email");
        userCreateDto.setPassword("Password@123");

        // When & Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");

        UserCreateDto loginDto = new UserCreateDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("Password@123");

        // When & Then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginWithWrongPassword() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");

        UserCreateDto loginDto = new UserCreateDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("WrongPassword");

        // When & Then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetUserByEmailSuccessfully() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");

        // When & Then
        mockMvc.perform(get("/user")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("updateduser");

        // When & Then
        mockMvc.perform(put("/user")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void shouldUpdatePasswordSuccessfully() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        UserPasswordUpdateDto passwordDto = new UserPasswordUpdateDto();
        passwordDto.setOldPassword("Password@123");
        passwordDto.setNewPassword("NewPassword@456");

        // When & Then
        mockMvc.perform(put("/user/password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(passwordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Password updated successfully")));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(post("/user/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logout successful")));
    }

    @Test
    void shouldValidateTokenSuccessfully() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(post("/user/validate-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessingProtectedEndpointWithoutToken() throws Exception {
        // When & Then
        mockMvc.perform(put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private UserModel createTestUser(String email, String password) {
        UserModel user = UserModel.builder()
                .username("testuser")
                .email(email)
                .password(new BCryptPasswordEncoder().encode(password))
                .build();
        return userRepository.save(user);
    }
}
package com.totex.simplegymapp.infrastructure.security;

import com.totex.simplegymapp.base.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
class SecurityConfigTest extends BaseIntegrationTest {

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        // Health check endpoint
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // User registration endpoint
        String userJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        // Login endpoint
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointsWithoutToken() throws Exception {
        // User endpoints
        mockMvc.perform(get("/user")
                        .param("email", "test@example.com"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isUnauthorized());

        // Workout endpoints
        mockMvc.perform(get("/workouts/my-workouts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // Exercise endpoints
        mockMvc.perform(get("/exercises/workout/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithInvalidToken() throws Exception {
        String invalidToken = "Bearer invalid-token-here";

        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/exercises/workout/1")
                        .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithMalformedToken() throws Exception {
        // Token without "Bearer " prefix
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", "malformed-token"))
                .andExpect(status().isUnauthorized());

        // Empty token
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", ""))
                .andExpect(status().isUnauthorized());

        // Only "Bearer " without token
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessWithValidToken() throws Exception {
        // Create user and get token
        String userJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "Password@123"
                }
                """;

        // Register user
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        // Login and get token
        String token = generateToken("test@example.com");

        // Test protected endpoints with valid token
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/user/validate-token")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleCorsRequests() throws Exception {
        // OPTIONS request (CORS preflight)
        mockMvc.perform(options("/user/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSecureAllWorkoutEndpoints() throws Exception {
        mockMvc.perform(get("/workouts/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/workouts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/workouts/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/workouts/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSecureAllExerciseEndpoints() throws Exception {
        mockMvc.perform(get("/exercises/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/exercises/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/exercises/all"))
                .andExpect(status().isUnauthorized());
    }
}
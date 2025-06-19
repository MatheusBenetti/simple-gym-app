package com.totex.simplegymapp.controller;

import com.totex.simplegymapp.base.BaseIntegrationTest;
import com.totex.simplegymapp.business.dto.WorkoutDto;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class WorkoutControllerTest extends BaseIntegrationTest {

    @Test
    void shouldCreateWorkoutSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        WorkoutDto workoutDto = new WorkoutDto();
        workoutDto.setWorkoutName("Treino de Peito");
        workoutDto.setStartDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/workouts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(workoutDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workoutName", is("Treino de Peito")))
                .andExpect(jsonPath("$.workoutId").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingWorkoutWithoutToken() throws Exception {
        // Given
        WorkoutDto workoutDto = new WorkoutDto();
        workoutDto.setWorkoutName("Treino de Peito");
        workoutDto.setStartDate(LocalDate.now());

        // When & Then
        mockMvc.perform(post("/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(workoutDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetMyWorkoutsSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        createTestWorkout(user, "Treino A - Peito");
        createTestWorkout(user, "Treino B - Costas");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].workoutName", is("Treino A - Peito")))
                .andExpect(jsonPath("$[1].workoutName", is("Treino B - Costas")));
    }

    @Test
    void shouldGetWorkoutByIdSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Peito");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/workouts/" + workout.getWorkoutId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName", is("Treino de Peito")))
                .andExpect(jsonPath("$.workoutId", is(workout.getWorkoutId().intValue())));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentWorkout() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/workouts/999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateWorkoutSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino Original");
        String token = generateToken("test@example.com");

        WorkoutDto updateDto = new WorkoutDto();
        updateDto.setWorkoutName("Treino Atualizado");

        // When & Then
        mockMvc.perform(put("/workouts/" + workout.getWorkoutId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName", is("Treino Atualizado")));
    }

    @Test
    void shouldDeleteWorkoutSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino para Deletar");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(delete("/workouts/" + workout.getWorkoutId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        // Verify workout was deleted
        mockMvc.perform(get("/workouts/" + workout.getWorkoutId())
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenTryingToAccessAnotherUsersWorkout() throws Exception {
        // Given
        UserModel user1 = createTestUser("user1@example.com", "Password@123");
        UserModel user2 = createTestUser("user2@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user1, "Treino do User1");
        String tokenUser2 = generateToken("user2@example.com");

        // When & Then
        mockMvc.perform(get("/workouts/" + workout.getWorkoutId())
                        .header("Authorization", tokenUser2))
                .andExpect(status().isNotFound()); // Returns not found for security
    }

    @Test
    void shouldGetAllWorkoutsSuccessfully() throws Exception {
        // Given
        UserModel user1 = createTestUser("user1@example.com", "Password@123");
        UserModel user2 = createTestUser("user2@example.com", "Password@123");
        createTestWorkout(user1, "Treino User1");
        createTestWorkout(user2, "Treino User2");
        String token = generateToken("user1@example.com");

        // When & Then
        mockMvc.perform(get("/workouts/all")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    private UserModel createTestUser(String email, String password) {
        UserModel user = UserModel.builder()
                .username("testuser")
                .email(email)
                .password(new BCryptPasswordEncoder().encode(password))
                .build();
        return userRepository.save(user);
    }

    private WorkoutModel createTestWorkout(UserModel user, String workoutName) {
        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName(workoutName);
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        return workoutRepository.save(workout);
    }
}
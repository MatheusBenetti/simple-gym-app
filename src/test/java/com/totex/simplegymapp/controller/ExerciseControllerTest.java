package com.totex.simplegymapp.controller;

import com.totex.simplegymapp.base.BaseIntegrationTest;
import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
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
class ExerciseControllerTest extends BaseIntegrationTest {

    @Test
    void shouldCreateExerciseSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Peito");
        String token = generateToken("test@example.com");

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("Supino Reto");
        exerciseDto.setSeries(4);
        exerciseDto.setRepetitions(12);
        exerciseDto.setWorkoutId(workout.getWorkoutId());

        // When & Then
        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(exerciseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName", is("Supino Reto")))
                .andExpect(jsonPath("$.series", is(4)))
                .andExpect(jsonPath("$.repetitions", is(12)))
                .andExpect(jsonPath("$.exerciseId").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingExerciseWithoutToken() throws Exception {
        // Given
        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("Supino Reto");
        exerciseDto.setSeries(4);
        exerciseDto.setRepetitions(12);
        exerciseDto.setWorkoutId(1L);

        // When & Then
        mockMvc.perform(post("/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(exerciseDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetExercisesByWorkoutSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Peito");
        createTestExercise(workout, "Supino Reto", 4, 12);
        createTestExercise(workout, "Supino Inclinado", 3, 10);
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/exercises/workout/" + workout.getWorkoutId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].exerciseName", is("Supino Reto")))
                .andExpect(jsonPath("$[1].exerciseName", is("Supino Inclinado")));
    }

    @Test
    void shouldGetExerciseByIdSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Peito");
        ExerciseModel exercise = createTestExercise(workout, "Supino Reto", 4, 12);
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/exercises/" + exercise.getExerciseId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseName", is("Supino Reto")))
                .andExpect(jsonPath("$.series", is(4)))
                .andExpect(jsonPath("$.repetitions", is(12)));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentExercise() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/exercises/999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateExerciseSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Peito");
        ExerciseModel exercise = createTestExercise(workout, "Nome Original", 3, 10);
        String token = generateToken("test@example.com");

        ExerciseDto updateDto = new ExerciseDto();
        updateDto.setExerciseName("Nome Atualizado");
        updateDto.setSeries(4);
        updateDto.setRepetitions(12);

        // When & Then
        mockMvc.perform(put("/exercises/" + exercise.getExerciseId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseName", is("Nome Atualizado")))
                .andExpect(jsonPath("$.series", is(4)))
                .andExpect(jsonPath("$.repetitions", is(12)));
    }

    @Test
    void shouldDeleteExerciseSuccessfully() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Peito");
        ExerciseModel exercise = createTestExercise(workout, "Exercício para Deletar", 3, 10);
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(delete("/exercises/" + exercise.getExerciseId())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        // Verify exercise was deleted
        mockMvc.perform(get("/exercises/" + exercise.getExerciseId())
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenTryingToAccessAnotherUsersExercise() throws Exception {
        // Given
        UserModel user1 = createTestUser("user1@example.com", "Password@123");
        UserModel user2 = createTestUser("user2@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user1, "Treino do User1");
        ExerciseModel exercise = createTestExercise(workout, "Exercício do User1", 4, 12);
        String tokenUser2 = generateToken("user2@example.com");

        // When & Then
        mockMvc.perform(get("/exercises/" + exercise.getExerciseId())
                        .header("Authorization", tokenUser2))
                .andExpect(status().isNotFound()); // Returns not found for security
    }

    @Test
    void shouldReturnNotFoundWhenCreatingExerciseForNonExistentWorkout() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("Supino Reto");
        exerciseDto.setSeries(4);
        exerciseDto.setRepetitions(12);
        exerciseDto.setWorkoutId(999L); // Non-existent workout

        // When & Then
        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(exerciseDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenGettingExercisesFromNonExistentWorkout() throws Exception {
        // Given
        createTestUser("test@example.com", "Password@123");
        String token = generateToken("test@example.com");

        // When & Then
        mockMvc.perform(get("/exercises/workout/999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllExercisesSuccessfully() throws Exception {
        // Given
        UserModel user1 = createTestUser("user1@example.com", "Password@123");
        UserModel user2 = createTestUser("user2@example.com", "Password@123");
        WorkoutModel workout1 = createTestWorkout(user1, "Treino User1");
        WorkoutModel workout2 = createTestWorkout(user2, "Treino User2");
        createTestExercise(workout1, "Exercício User1", 4, 12);
        createTestExercise(workout2, "Exercício User2", 3, 10);
        String token = generateToken("user1@example.com");

        // When & Then
        mockMvc.perform(get("/exercises/all")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldHandleExerciseWithZeroValues() throws Exception {
        // Given
        UserModel user = createTestUser("test@example.com", "Password@123");
        WorkoutModel workout = createTestWorkout(user, "Treino de Teste");
        String token = generateToken("test@example.com");

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("Exercício Zero");
        exerciseDto.setSeries(0);
        exerciseDto.setRepetitions(0);
        exerciseDto.setWorkoutId(workout.getWorkoutId());

        // When & Then
        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(exerciseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName", is("Exercício Zero")))
                .andExpect(jsonPath("$.series", is(0)))
                .andExpect(jsonPath("$.repetitions", is(0)));
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

    private ExerciseModel createTestExercise(WorkoutModel workout, String exerciseName, int series, int repetitions) {
        ExerciseModel exercise = new ExerciseModel();
        exercise.setExerciseName(exerciseName);
        exercise.setSeries(series);
        exercise.setRepetitions(repetitions);
        exercise.setWorkout(workout);
        return exerciseRepository.save(exercise);
    }
}
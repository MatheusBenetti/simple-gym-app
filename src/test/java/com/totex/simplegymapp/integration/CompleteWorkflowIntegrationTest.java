package com.totex.simplegymapp.integration;

import com.totex.simplegymapp.base.BaseIntegrationTest;
import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.WorkoutDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integração completo que simula o fluxo completo de um usuário:
 * 1. Registrar usuário
 * 2. Fazer login
 * 3. Criar treinos
 * 4. Adicionar exercícios
 * 5. Consultar dados
 * 6. Atualizar informações
 * 7. Deletar dados
 */
@AutoConfigureWebMvc
class CompleteWorkflowIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldExecuteCompleteUserWorkflowSuccessfully() throws Exception {
        // 1. Registrar usuário
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername("joao");
        userCreateDto.setEmail("joao@gym.com");
        userCreateDto.setPassword("MinhaSenh@123");

        MvcResult registerResult = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("joao")))
                .andExpect(jsonPath("$.email", is("joao@gym.com")))
                .andReturn();

        // 2. Fazer login
        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andReturn();

        String tokenResponse = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(tokenResponse).get("token").asText();

        // 3. Criar treino de peito
        WorkoutDto peitoWorkout = new WorkoutDto();
        peitoWorkout.setWorkoutName("Treino A - Peito e Tríceps");
        peitoWorkout.setStartDate(LocalDate.now());

        MvcResult workoutResult = mockMvc.perform(post("/workouts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(peitoWorkout)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workoutName", is("Treino A - Peito e Tríceps")))
                .andExpect(jsonPath("$.workoutId").exists())
                .andReturn();

        String workoutResponse = workoutResult.getResponse().getContentAsString();
        Long workoutId = objectMapper.readTree(workoutResponse).get("workoutId").asLong();

        // 4. Adicionar exercícios ao treino
        // 4.1 Supino Reto
        ExerciseDto supinoReto = new ExerciseDto();
        supinoReto.setExerciseName("Supino Reto");
        supinoReto.setSeries(4);
        supinoReto.setRepetitions(10);
        supinoReto.setWorkoutId(workoutId);

        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(supinoReto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName", is("Supino Reto")))
                .andExpect(jsonPath("$.series", is(4)))
                .andExpect(jsonPath("$.repetitions", is(10)));

        // 4.2 Supino Inclinado
        ExerciseDto supinoInclinado = new ExerciseDto();
        supinoInclinado.setExerciseName("Supino Inclinado");
        supinoInclinado.setSeries(3);
        supinoInclinado.setRepetitions(12);
        supinoInclinado.setWorkoutId(workoutId);

        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(supinoInclinado)))
                .andExpect(status().isCreated());

        // 4.3 Tríceps Pulley
        ExerciseDto tricepsPulley = new ExerciseDto();
        tricepsPulley.setExerciseName("Tríceps Pulley");
        tricepsPulley.setSeries(3);
        tricepsPulley.setRepetitions(15);
        tricepsPulley.setWorkoutId(workoutId);

        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(tricepsPulley)))
                .andExpect(status().isCreated());

        // 5. Criar treino de costas
        WorkoutDto costasWorkout = new WorkoutDto();
        costasWorkout.setWorkoutName("Treino B - Costas e Bíceps");
        costasWorkout.setStartDate(LocalDate.now().plusDays(1));

        MvcResult costasResult = mockMvc.perform(post("/workouts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(costasWorkout)))
                .andExpect(status().isCreated())
                .andReturn();

        String costasResponse = costasResult.getResponse().getContentAsString();
        Long costasWorkoutId = objectMapper.readTree(costasResponse).get("workoutId").asLong();

        // 6. Adicionar exercício ao treino de costas
        ExerciseDto puxadaFrontal = new ExerciseDto();
        puxadaFrontal.setExerciseName("Puxada Frontal");
        puxadaFrontal.setSeries(4);
        puxadaFrontal.setRepetitions(12);
        puxadaFrontal.setWorkoutId(costasWorkoutId);

        mockMvc.perform(post("/exercises")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(puxadaFrontal)))
                .andExpect(status().isCreated());

        // 7. Consultar todos os treinos do usuário
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].workoutName",
                        containsInAnyOrder("Treino A - Peito e Tríceps", "Treino B - Costas e Bíceps")));

        // 8. Consultar exercícios do treino de peito
        mockMvc.perform(get("/exercises/workout/" + workoutId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].exerciseName",
                        containsInAnyOrder("Supino Reto", "Supino Inclinado", "Tríceps Pulley")));

        // 9. Consultar treino específico
        mockMvc.perform(get("/workouts/" + workoutId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName", is("Treino A - Peito e Tríceps")))
                .andExpect(jsonPath("$.workoutId", is(workoutId.intValue())));

        // 10. Atualizar nome do treino
        WorkoutDto updateWorkout = new WorkoutDto();
        updateWorkout.setWorkoutName("Treino A - Peito, Ombro e Tríceps");

        mockMvc.perform(put("/workouts/" + workoutId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateWorkout)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName", is("Treino A - Peito, Ombro e Tríceps")));

        // 11. Validar token
        mockMvc.perform(post("/user/validate-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.email", is("joao@gym.com")));

        // 12. Fazer logout
        mockMvc.perform(post("/user/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logout successful")));

        // 13. Tentar acessar dados após logout (deve falhar)
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPreventAccessToBetweenDifferentUsers() throws Exception {
        // Criar primeiro usuário
        UserCreateDto user1 = new UserCreateDto();
        user1.setUsername("user1");
        user1.setEmail("user1@gym.com");
        user1.setPassword("Password@123");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user1)))
                .andExpect(status().isOk());

        MvcResult login1 = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user1)))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(login1.getResponse().getContentAsString())
                .get("token").asText();

        // Criar segundo usuário
        UserCreateDto user2 = new UserCreateDto();
        user2.setUsername("user2");
        user2.setEmail("user2@gym.com");
        user2.setPassword("Password@123");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user2)))
                .andExpect(status().isOk());

        MvcResult login2 = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user2)))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(login2.getResponse().getContentAsString())
                .get("token").asText();

        // User1 cria um treino
        WorkoutDto workout = new WorkoutDto();
        workout.setWorkoutName("Treino Privado User1");
        workout.setStartDate(LocalDate.now());

        MvcResult workoutResult = mockMvc.perform(post("/workouts")
                        .header("Authorization", token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(workout)))
                .andExpect(status().isCreated())
                .andReturn();

        Long workoutId = objectMapper.readTree(workoutResult.getResponse().getContentAsString())
                .get("workoutId").asLong();

        // User2 tenta acessar treino do User1 (deve falhar)
        mockMvc.perform(get("/workouts/" + workoutId)
                        .header("Authorization", token2))
                .andExpect(status().isNotFound()); // Security: returns 404 instead of 403

        // User2 não deve ver treinos do User1 na lista
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldHandleInvalidDataGracefully() throws Exception {
        // Tentar criar usuário com email inválido
        UserCreateDto invalidUser = new UserCreateDto();
        invalidUser.setUsername("test");
        invalidUser.setEmail("invalid-email");
        invalidUser.setPassword("Password@123");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Tentar criar usuário com senha fraca
        UserCreateDto weakPassword = new UserCreateDto();
        weakPassword.setUsername("test");
        weakPassword.setEmail("test@example.com");
        weakPassword.setPassword("123");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(weakPassword)))
                .andExpect(status().isBadRequest());

        // Tentar acessar endpoint protegido sem token
        mockMvc.perform(get("/workouts/my-workouts"))
                .andExpect(status().isUnauthorized());

        // Tentar usar token inválido
        mockMvc.perform(get("/workouts/my-workouts")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
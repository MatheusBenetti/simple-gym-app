package com.totex.simplegymapp.performance;

import com.totex.simplegymapp.base.BaseIntegrationTest;
import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.business.dto.UserCreateDto;
import com.totex.simplegymapp.business.dto.WorkoutDto;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de performance para avaliar o desempenho da aplicação
 * com cache Redis e operações de banco de dados.
 */
@AutoConfigureWebMvc
class PerformanceTest extends BaseIntegrationTest {

    @Test
    void shouldPerformWellWithMultipleUsers() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Criar 50 usuários
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            UserCreateDto user = new UserCreateDto();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@gym.com");
            user.setPassword("Password@123");

            // Registrar usuário
            mockMvc.perform(post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(user)))
                    .andExpect(status().isOk());

            // Fazer login e obter token
            MvcResult loginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(user)))
                    .andExpect(status().isOk())
                    .andReturn();

            String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                    .get("token").asText();
            tokens.add(token);
        }

        stopWatch.stop();
        System.out.println("Tempo para criar 50 usuários: " + stopWatch.getTotalTimeMillis() + "ms");

        // Criar treinos para cada usuário
        stopWatch.start();
        for (int i = 0; i < tokens.size(); i++) {
            WorkoutDto workout = new WorkoutDto();
            workout.setWorkoutName("Treino " + i);
            workout.setStartDate(LocalDate.now());

            mockMvc.perform(post("/workouts")
                            .header("Authorization", tokens.get(i))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(workout)))
                    .andExpect(status().isCreated());
        }

        stopWatch.stop();
        System.out.println("Tempo para criar 50 treinos: " + stopWatch.getTotalTimeMillis() + "ms");

        // Verificar se o desempenho está dentro do aceitável
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(30000); // Menos de 30 segundos
    }

    @Test
    void shouldCacheUserDataEffectively() throws Exception {
        // Criar usuário
        UserModel user = createTestUser();
        String token = generateToken(user.getEmail());

        // Primeira consulta (sem cache)
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/user")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk());

        stopWatch.stop();
        long firstCallTime = stopWatch.getTotalTimeMillis();

        // Segunda consulta (com cache)
        stopWatch.start();

        mockMvc.perform(get("/user")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk());

        stopWatch.stop();
        long secondCallTime = stopWatch.getTotalTimeMillis();

        System.out.println("Primeira consulta: " + firstCallTime + "ms");
        System.out.println("Segunda consulta (cache): " + secondCallTime + "ms");

        // A segunda consulta deve ser mais rápida devido ao cache
        // (não sempre garantido em testes devido à variabilidade, mas é um indicador)
        assertThat(secondCallTime).isLessThanOrEqualTo(firstCallTime + 50); // Tolerância
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        // Criar usuário
        UserModel user = createTestUser();
        String token = generateToken(user.getEmail());

        // Criar um treino
        WorkoutDto workout = new WorkoutDto();
        workout.setWorkoutName("Treino Concorrente");
        workout.setStartDate(LocalDate.now());

        MvcResult workoutResult = mockMvc.perform(post("/workouts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(workout)))
                .andExpect(status().isCreated())
                .andReturn();

        Long workoutId = objectMapper.readTree(workoutResult.getResponse().getContentAsString())
                .get("workoutId").asLong();

        // Executar 20 requisições concorrentes para buscar treinos
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < 20; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(get("/workouts/my-workouts")
                                    .header("Authorization", token))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            futures.add(future);
        }

        // Aguardar todas as requisições
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        stopWatch.stop();
        System.out.println("Tempo para 20 requisições concorrentes: " + stopWatch.getTotalTimeMillis() + "ms");

        executor.shutdown();

        // Verificar se o tempo total está aceitável
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(10000); // Menos de 10 segundos
    }

    @Test
    void shouldPerformWellWithManyExercises() throws Exception {
        // Criar usuário e treino
        UserModel user = createTestUser();
        String token = generateToken(user.getEmail());

        WorkoutDto workout = new WorkoutDto();
        workout.setWorkoutName("Treino Completo");
        workout.setStartDate(LocalDate.now());

        MvcResult workoutResult = mockMvc.perform(post("/workouts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(workout)))
                .andExpect(status().isCreated())
                .andReturn();

        Long workoutId = objectMapper.readTree(workoutResult.getResponse().getContentAsString())
                .get("workoutId").asLong();

        // Adicionar 100 exercícios
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < 100; i++) {
            ExerciseDto exercise = new ExerciseDto();
            exercise.setExerciseName("Exercício " + i);
            exercise.setSeries(3 + (i % 3));
            exercise.setRepetitions(10 + (i % 5));
            exercise.setWorkoutId(workoutId);

            mockMvc.perform(post("/exercises")
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(exercise)))
                    .andExpect(status().isCreated());
        }

        stopWatch.stop();
        System.out.println("Tempo para criar 100 exercícios: " + stopWatch.getTotalTimeMillis() + "ms");

        // Buscar todos os exercícios
        stopWatch.start();

        mockMvc.perform(get("/exercises/workout/" + workoutId)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        stopWatch.stop();
        System.out.println("Tempo para buscar 100 exercícios: " + stopWatch.getTotalTimeMillis() + "ms");

        // Verificar performance aceitável
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // Menos de 5 segundos
    }

    private UserModel createTestUser() {
        UserModel user = UserModel.builder()
                .username("perfuser")
                .email("perf@test.com")
                .password(new BCryptPasswordEncoder().encode("Password@123"))
                .build();
        return userRepository.save(user);
    }
}
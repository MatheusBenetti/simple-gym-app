package com.totex.simplegymapp.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totex.simplegymapp.infrastructure.config.EmbeddedRedisConfig;
import com.totex.simplegymapp.infrastructure.repository.UserRepository;
import com.totex.simplegymapp.infrastructure.repository.WorkoutRepository;
import com.totex.simplegymapp.infrastructure.repository.ExerciseRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Import(EmbeddedRedisConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected WorkoutRepository workoutRepository;

    @Autowired
    protected ExerciseRepository exerciseRepository;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        exerciseRepository.deleteAll();
        workoutRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String generateToken(String email) {
        return "Bearer " + jwtUtil.generateToken(email);
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
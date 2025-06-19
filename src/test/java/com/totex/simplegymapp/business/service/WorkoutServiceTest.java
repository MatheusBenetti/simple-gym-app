package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.WorkoutConverter;
import com.totex.simplegymapp.business.dto.WorkoutDto;
import com.totex.simplegymapp.infrastructure.exception.ResourceNotFoundException;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import com.totex.simplegymapp.infrastructure.repository.UserRepository;
import com.totex.simplegymapp.infrastructure.repository.WorkoutRepository;
import com.totex.simplegymapp.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutConverter workoutConverter;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private WorkoutService workoutService;

    private UserModel userModel;
    private WorkoutModel workoutModel;
    private WorkoutDto workoutDto;
    private String token;

    @BeforeEach
    void setUp() {
        token = "Bearer validtoken";

        userModel = UserModel.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        workoutModel = new WorkoutModel();
        workoutModel.setWorkoutId(1L);
        workoutModel.setWorkoutName("Treino de Peito");
        workoutModel.setStartDate(LocalDate.now());
        workoutModel.setUser(userModel);

        workoutDto = new WorkoutDto();
        workoutDto.setWorkoutId(1L);
        workoutDto.setWorkoutName("Treino de Peito");
        workoutDto.setStartDate(LocalDate.now());
        workoutDto.setUserId(1L);
    }

    @Test
    void shouldCreateWorkoutSuccessfully() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(userModel));
        given(workoutConverter.toWorkoutModel(workoutDto, userModel)).willReturn(workoutModel);
        given(workoutRepository.save(workoutModel)).willReturn(workoutModel);
        given(workoutConverter.toWorkoutDto(workoutModel)).willReturn(workoutDto);

        // When
        WorkoutDto result = workoutService.createWorkout(token, workoutDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWorkoutName()).isEqualTo("Treino de Peito");
        verify(workoutRepository).save(workoutModel);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserNotFoundForWorkoutCreation() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutService.createWorkout(token, workoutDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found.");
    }

    @Test
    void shouldGetUserWorkoutsSuccessfully() {
        // Given
        userModel.setWorkouts(Arrays.asList(workoutModel));
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(userModel));
        given(workoutConverter.toWorkoutDto(workoutModel)).willReturn(workoutDto);

        // When
        List<WorkoutDto> result = workoutService.getUserWorkouts(token);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWorkoutName()).isEqualTo("Treino de Peito");
    }

    @Test
    void shouldGetWorkoutByIdSuccessfully() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));
        given(workoutConverter.toWorkoutDto(workoutModel)).willReturn(workoutDto);

        // When
        WorkoutDto result = workoutService.getWorkoutById(1L, token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWorkoutName()).isEqualTo("Treino de Peito");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutNotFound() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutService.getWorkoutById(1L, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workout not found.");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotBelongToUser() {
        // Given
        UserModel anotherUser = UserModel.builder()
                .userId(2L)
                .email("another@example.com")
                .build();
        workoutModel.setUser(anotherUser);

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));

        // When & Then
        assertThatThrownBy(() -> workoutService.getWorkoutById(1L, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workout not found.");
    }

    @Test
    void shouldUpdateWorkoutSuccessfully() {
        // Given
        WorkoutDto updateDto = new WorkoutDto();
        updateDto.setWorkoutName("Treino de Peito Atualizado");
        updateDto.setStartDate(LocalDate.now().plusDays(1));

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));
        given(workoutRepository.save(any(WorkoutModel.class))).willReturn(workoutModel);
        given(workoutConverter.toWorkoutDto(workoutModel)).willReturn(workoutDto);

        // When
        WorkoutDto result = workoutService.updateWorkout(1L, updateDto, token);

        // Then
        assertThat(result).isNotNull();
        verify(workoutRepository).save(workoutModel);
    }

    @Test
    void shouldDeleteWorkoutSuccessfully() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));

        // When
        workoutService.deleteWorkout(1L, token);

        // Then
        verify(workoutRepository).delete(workoutModel);
    }
}
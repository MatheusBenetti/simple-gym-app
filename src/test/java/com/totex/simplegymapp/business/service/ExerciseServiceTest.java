package com.totex.simplegymapp.business.service;

import com.totex.simplegymapp.business.converter.ExerciseConverter;
import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.infrastructure.exception.ResourceNotFoundException;
import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import com.totex.simplegymapp.infrastructure.repository.ExerciseRepository;
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
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseConverter exerciseConverter;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ExerciseService exerciseService;

    private UserModel userModel;
    private WorkoutModel workoutModel;
    private ExerciseModel exerciseModel;
    private ExerciseDto exerciseDto;
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

        exerciseModel = new ExerciseModel();
        exerciseModel.setExerciseId(1L);
        exerciseModel.setExerciseName("Supino Reto");
        exerciseModel.setSeries(4);
        exerciseModel.setRepetitions(12);
        exerciseModel.setWorkout(workoutModel);

        exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseId(1L);
        exerciseDto.setExerciseName("Supino Reto");
        exerciseDto.setSeries(4);
        exerciseDto.setRepetitions(12);
        exerciseDto.setWorkoutId(1L);
    }

    @Test
    void shouldCreateExerciseSuccessfully() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));
        given(exerciseConverter.toExerciseModel(exerciseDto, workoutModel)).willReturn(exerciseModel);
        given(exerciseRepository.save(exerciseModel)).willReturn(exerciseModel);
        given(exerciseConverter.toExerciseDto(exerciseModel)).willReturn(exerciseDto);

        // When
        ExerciseDto result = exerciseService.createExercise(exerciseDto, token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExerciseName()).isEqualTo("Supino Reto");
        assertThat(result.getSeries()).isEqualTo(4);
        assertThat(result.getRepetitions()).isEqualTo(12);
        verify(exerciseRepository).save(exerciseModel);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutNotFoundForExerciseCreation() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exerciseService.createExercise(exerciseDto, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workout not found.");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenWorkoutDoesNotBelongToUserForExerciseCreation() {
        // Given
        UserModel anotherUser = UserModel.builder()
                .userId(2L)
                .email("another@example.com")
                .build();
        workoutModel.setUser(anotherUser);

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));

        // When & Then
        assertThatThrownBy(() -> exerciseService.createExercise(exerciseDto, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workout not found.");
    }

    @Test
    void shouldGetExercisesByWorkoutSuccessfully() {
        // Given
        workoutModel.setExercises(Arrays.asList(exerciseModel));
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(workoutRepository.findById(1L)).willReturn(Optional.of(workoutModel));
        given(exerciseConverter.toExerciseDto(exerciseModel)).willReturn(exerciseDto);

        // When
        List<ExerciseDto> result = exerciseService.getExercisesByWorkout(1L, token);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExerciseName()).isEqualTo("Supino Reto");
    }

    @Test
    void shouldGetExerciseByIdSuccessfully() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(exerciseRepository.findById(1L)).willReturn(Optional.of(exerciseModel));
        given(exerciseConverter.toExerciseDto(exerciseModel)).willReturn(exerciseDto);

        // When
        ExerciseDto result = exerciseService.getExerciseById(1L, token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExerciseName()).isEqualTo("Supino Reto");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenExerciseNotFound() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(exerciseRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exerciseService.getExerciseById(1L, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exercise not found.");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenExerciseDoesNotBelongToUser() {
        // Given
        UserModel anotherUser = UserModel.builder()
                .userId(2L)
                .email("another@example.com")
                .build();
        workoutModel.setUser(anotherUser);

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(exerciseRepository.findById(1L)).willReturn(Optional.of(exerciseModel));

        // When & Then
        assertThatThrownBy(() -> exerciseService.getExerciseById(1L, token))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exercise not found.");
    }

    @Test
    void shouldUpdateExerciseSuccessfully() {
        // Given
        ExerciseDto updateDto = new ExerciseDto();
        updateDto.setExerciseName("Supino Inclinado");
        updateDto.setSeries(3);
        updateDto.setRepetitions(10);

        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(exerciseRepository.findById(1L)).willReturn(Optional.of(exerciseModel));
        given(exerciseRepository.save(any(ExerciseModel.class))).willReturn(exerciseModel);
        given(exerciseConverter.toExerciseDto(exerciseModel)).willReturn(exerciseDto);

        // When
        ExerciseDto result = exerciseService.updateExercise(1L, updateDto, token);

        // Then
        assertThat(result).isNotNull();
        verify(exerciseRepository).save(exerciseModel);
    }

    @Test
    void shouldDeleteExerciseSuccessfully() {
        // Given
        given(jwtUtil.extractEmailToken("validtoken")).willReturn("test@example.com");
        given(exerciseRepository.findById(1L)).willReturn(Optional.of(exerciseModel));

        // When
        exerciseService.deleteExercise(1L, token);

        // Then
        verify(exerciseRepository).delete(exerciseModel);
    }

    @Test
    void shouldGetAllExercisesSuccessfully() {
        // Given
        given(exerciseRepository.findAll()).willReturn(Arrays.asList(exerciseModel));
        given(exerciseConverter.toExerciseDto(exerciseModel)).willReturn(exerciseDto);

        // When
        List<ExerciseDto> result = exerciseService.getAllExercises();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExerciseName()).isEqualTo("Supino Reto");
    }
}
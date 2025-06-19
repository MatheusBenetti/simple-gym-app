package com.totex.simplegymapp.business.converter;

import com.totex.simplegymapp.business.dto.ExerciseDto;
import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class ExerciseConverterTest {

    @InjectMocks
    private ExerciseConverter exerciseConverter;

    private ExerciseModel exerciseModel;
    private ExerciseDto exerciseDto;
    private WorkoutModel workoutModel;

    @BeforeEach
    void setUp() {
        UserModel userModel = UserModel.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        workoutModel = new WorkoutModel();
        workoutModel.setWorkoutId(1L);
        workoutModel.setWorkoutName("Treino de Peito");
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
    void shouldConvertExerciseModelToExerciseDto() {
        // When
        ExerciseDto result = exerciseConverter.toExerciseDto(exerciseModel);

        // Then
        assertThat(result.getExerciseId()).isEqualTo(1L);
        assertThat(result.getExerciseName()).isEqualTo("Supino Reto");
        assertThat(result.getSeries()).isEqualTo(4);
        assertThat(result.getRepetitions()).isEqualTo(12);
        assertThat(result.getWorkoutId()).isEqualTo(1L);
    }

    @Test
    void shouldConvertExerciseDtoToExerciseModel() {
        // When
        ExerciseModel result = exerciseConverter.toExerciseModel(exerciseDto, workoutModel);

        // Then
        assertThat(result.getExerciseId()).isEqualTo(1L);
        assertThat(result.getExerciseName()).isEqualTo("Supino Reto");
        assertThat(result.getSeries()).isEqualTo(4);
        assertThat(result.getRepetitions()).isEqualTo(12);
        assertThat(result.getWorkout()).isEqualTo(workoutModel);
    }

    @Test
    void shouldHandleZeroValues() {
        // Given
        exerciseDto.setSeries(0);
        exerciseDto.setRepetitions(0);

        // When
        ExerciseModel result = exerciseConverter.toExerciseModel(exerciseDto, workoutModel);

        // Then
        assertThat(result.getSeries()).isEqualTo(0);
        assertThat(result.getRepetitions()).isEqualTo(0);
        assertThat(result.getWorkout()).isEqualTo(workoutModel);
    }

    @Test
    void shouldHandleNullExerciseName() {
        // Given
        exerciseDto.setExerciseName(null);

        // When
        ExerciseModel result = exerciseConverter.toExerciseModel(exerciseDto, workoutModel);

        // Then
        assertThat(result.getExerciseName()).isNull();
        assertThat(result.getWorkout()).isEqualTo(workoutModel);
    }

    @Test
    void shouldConvertExerciseModelWithNullWorkout() {
        // Given
        exerciseModel.setWorkout(null);

        // When & Then
        assertThatCode(() -> exerciseConverter.toExerciseDto(exerciseModel))
                .doesNotThrowAnyException();
    }
}
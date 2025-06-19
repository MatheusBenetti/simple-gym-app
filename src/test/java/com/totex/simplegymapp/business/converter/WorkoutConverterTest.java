package com.totex.simplegymapp.business.converter;

import com.totex.simplegymapp.business.dto.WorkoutDto;
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
class WorkoutConverterTest {

    @InjectMocks
    private WorkoutConverter workoutConverter;

    private WorkoutModel workoutModel;
    private WorkoutDto workoutDto;
    private UserModel userModel;

    @BeforeEach
    void setUp() {
        userModel = UserModel.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        workoutModel = new WorkoutModel();
        workoutModel.setWorkoutId(1L);
        workoutModel.setWorkoutName("Treino de Peito");
        workoutModel.setStartDate(LocalDate.of(2025, 6, 19));
        workoutModel.setUser(userModel);

        workoutDto = new WorkoutDto();
        workoutDto.setWorkoutId(1L);
        workoutDto.setWorkoutName("Treino de Peito");
        workoutDto.setStartDate(LocalDate.of(2025, 6, 19));
        workoutDto.setUserId(1L);
        workoutDto.setUsername("testuser");
    }

    @Test
    void shouldConvertWorkoutModelToWorkoutDto() {
        // When
        WorkoutDto result = workoutConverter.toWorkoutDto(workoutModel);

        // Then
        assertThat(result.getWorkoutId()).isEqualTo(1L);
        assertThat(result.getWorkoutName()).isEqualTo("Treino de Peito");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 6, 19));
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldConvertWorkoutDtoToWorkoutModel() {
        // When
        WorkoutModel result = workoutConverter.toWorkoutModel(workoutDto, userModel);

        // Then
        assertThat(result.getWorkoutName()).isEqualTo("Treino de Peito");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 6, 19));
        assertThat(result.getUser()).isEqualTo(userModel);
        assertThat(result.getWorkoutId()).isNull(); // ID é gerado pelo banco
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        WorkoutDto nullDto = new WorkoutDto();
        nullDto.setWorkoutName(null);
        nullDto.setStartDate(null);

        // When
        WorkoutModel result = workoutConverter.toWorkoutModel(nullDto, userModel);

        // Then
        assertThat(result.getWorkoutName()).isNull();
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getUser()).isEqualTo(userModel);
    }

    @Test
    void shouldConvertWorkoutModelWithNullUser() {
        // Given
        workoutModel.setUser(null);

        // When & Then
        assertThatCode(() -> workoutConverter.toWorkoutDto(workoutModel))
                .doesNotThrowAnyException();
    }
}
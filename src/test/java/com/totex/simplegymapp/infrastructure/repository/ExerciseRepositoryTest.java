package com.totex.simplegymapp.infrastructure.repository;

import com.totex.simplegymapp.infrastructure.model.ExerciseModel;
import com.totex.simplegymapp.infrastructure.model.UserModel;
import com.totex.simplegymapp.infrastructure.model.WorkoutModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExerciseRepositoryTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFindExercise() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino de Peito");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = entityManager.persistAndFlush(workout);

        ExerciseModel exercise = new ExerciseModel();
        exercise.setExerciseName("Supino Reto");
        exercise.setSeries(4);
        exercise.setRepetitions(12);
        exercise.setWorkout(workout);

        // When
        ExerciseModel savedExercise = exerciseRepository.save(exercise);
        Optional<ExerciseModel> foundExercise = exerciseRepository.findById(savedExercise.getExerciseId());

        // Then
        assertThat(savedExercise.getExerciseId()).isNotNull();
        assertThat(foundExercise).isPresent();
        assertThat(foundExercise.get().getExerciseName()).isEqualTo("Supino Reto");
        assertThat(foundExercise.get().getSeries()).isEqualTo(4);
        assertThat(foundExercise.get().getRepetitions()).isEqualTo(12);
        assertThat(foundExercise.get().getWorkout()).isEqualTo(workout);
    }

    @Test
    void shouldDeleteExercise() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino de Peito");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = entityManager.persistAndFlush(workout);

        ExerciseModel exercise = new ExerciseModel();
        exercise.setExerciseName("Exercício para Deletar");
        exercise.setSeries(3);
        exercise.setRepetitions(10);
        exercise.setWorkout(workout);
        exercise = exerciseRepository.save(exercise);

        // When
        exerciseRepository.delete(exercise);

        // Then
        Optional<ExerciseModel> foundExercise = exerciseRepository.findById(exercise.getExerciseId());
        assertThat(foundExercise).isEmpty();
    }

    @Test
    void shouldFindAllExercises() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino Completo");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = entityManager.persistAndFlush(workout);

        ExerciseModel exercise1 = new ExerciseModel();
        exercise1.setExerciseName("Supino Reto");
        exercise1.setSeries(4);
        exercise1.setRepetitions(12);
        exercise1.setWorkout(workout);

        ExerciseModel exercise2 = new ExerciseModel();
        exercise2.setExerciseName("Supino Inclinado");
        exercise2.setSeries(3);
        exercise2.setRepetitions(10);
        exercise2.setWorkout(workout);

        exerciseRepository.save(exercise1);
        exerciseRepository.save(exercise2);

        // When
        List<ExerciseModel> allExercises = exerciseRepository.findAll();

        // Then
        assertThat(allExercises).hasSize(2);
        assertThat(allExercises).extracting(ExerciseModel::getExerciseName)
                .containsExactlyInAnyOrder("Supino Reto", "Supino Inclinado");
    }

    @Test
    void shouldUpdateExercise() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino de Peito");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = entityManager.persistAndFlush(workout);

        ExerciseModel exercise = new ExerciseModel();
        exercise.setExerciseName("Nome Original");
        exercise.setSeries(3);
        exercise.setRepetitions(10);
        exercise.setWorkout(workout);
        exercise = exerciseRepository.save(exercise);

        // When
        exercise.setExerciseName("Nome Atualizado");
        exercise.setSeries(4);
        exercise.setRepetitions(12);
        ExerciseModel updatedExercise = exerciseRepository.save(exercise);

        // Then
        assertThat(updatedExercise.getExerciseName()).isEqualTo("Nome Atualizado");
        assertThat(updatedExercise.getSeries()).isEqualTo(4);
        assertThat(updatedExercise.getRepetitions()).isEqualTo(12);
        assertThat(updatedExercise.getExerciseId()).isEqualTo(exercise.getExerciseId());
    }

    @Test
    void shouldMaintainWorkoutRelationship() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino de Peito");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = entityManager.persistAndFlush(workout);

        ExerciseModel exercise = new ExerciseModel();
        exercise.setExerciseName("Supino com Relacionamento");
        exercise.setSeries(4);
        exercise.setRepetitions(12);
        exercise.setWorkout(workout);

        // When
        ExerciseModel savedExercise = exerciseRepository.save(exercise);

        // Then
        assertThat(savedExercise.getWorkout()).isNotNull();
        assertThat(savedExercise.getWorkout().getWorkoutName()).isEqualTo("Treino de Peito");
        assertThat(savedExercise.getWorkout().getWorkoutId()).isEqualTo(workout.getWorkoutId());
        assertThat(savedExercise.getWorkout().getUser()).isEqualTo(user);
    }

    @Test
    void shouldHandleExerciseWithZeroValues() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino de Teste");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = entityManager.persistAndFlush(workout);

        ExerciseModel exercise = new ExerciseModel();
        exercise.setExerciseName("Exercício Zero");
        exercise.setSeries(0);
        exercise.setRepetitions(0);
        exercise.setWorkout(workout);

        // When
        ExerciseModel savedExercise = exerciseRepository.save(exercise);

        // Then
        assertThat(savedExercise.getSeries()).isEqualTo(0);
        assertThat(savedExercise.getRepetitions()).isEqualTo(0);
        assertThat(savedExercise.getExerciseId()).isNotNull();
    }
}
package com.totex.simplegymapp.infrastructure.repository;

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
class WorkoutRepositoryTest {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFindWorkout() {
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

        // When
        WorkoutModel savedWorkout = workoutRepository.save(workout);
        Optional<WorkoutModel> foundWorkout = workoutRepository.findById(savedWorkout.getWorkoutId());

        // Then
        assertThat(savedWorkout.getWorkoutId()).isNotNull();
        assertThat(foundWorkout).isPresent();
        assertThat(foundWorkout.get().getWorkoutName()).isEqualTo("Treino de Peito");
        assertThat(foundWorkout.get().getUser()).isEqualTo(user);
    }

    @Test
    void shouldDeleteWorkout() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino para Deletar");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = workoutRepository.save(workout);

        // When
        workoutRepository.delete(workout);

        // Then
        Optional<WorkoutModel> foundWorkout = workoutRepository.findById(workout.getWorkoutId());
        assertThat(foundWorkout).isEmpty();
    }

    @Test
    void shouldFindAllWorkouts() {
        // Given
        UserModel user1 = UserModel.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password1")
                .build();
        user1 = entityManager.persistAndFlush(user1);

        UserModel user2 = UserModel.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password2")
                .build();
        user2 = entityManager.persistAndFlush(user2);

        WorkoutModel workout1 = new WorkoutModel();
        workout1.setWorkoutName("Treino 1");
        workout1.setStartDate(LocalDate.now());
        workout1.setUser(user1);

        WorkoutModel workout2 = new WorkoutModel();
        workout2.setWorkoutName("Treino 2");
        workout2.setStartDate(LocalDate.now());
        workout2.setUser(user2);

        workoutRepository.save(workout1);
        workoutRepository.save(workout2);

        // When
        List<WorkoutModel> allWorkouts = workoutRepository.findAll();

        // Then
        assertThat(allWorkouts).hasSize(2);
        assertThat(allWorkouts).extracting(WorkoutModel::getWorkoutName)
                .containsExactlyInAnyOrder("Treino 1", "Treino 2");
    }

    @Test
    void shouldUpdateWorkout() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Nome Original");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);
        workout = workoutRepository.save(workout);

        // When
        workout.setWorkoutName("Nome Atualizado");
        WorkoutModel updatedWorkout = workoutRepository.save(workout);

        // Then
        assertThat(updatedWorkout.getWorkoutName()).isEqualTo("Nome Atualizado");
        assertThat(updatedWorkout.getWorkoutId()).isEqualTo(workout.getWorkoutId());
    }

    @Test
    void shouldMaintainUserRelationship() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();
        user = entityManager.persistAndFlush(user);

        WorkoutModel workout = new WorkoutModel();
        workout.setWorkoutName("Treino com Usuário");
        workout.setStartDate(LocalDate.now());
        workout.setUser(user);

        // When
        WorkoutModel savedWorkout = workoutRepository.save(workout);

        // Then
        assertThat(savedWorkout.getUser()).isNotNull();
        assertThat(savedWorkout.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(savedWorkout.getUser().getUserId()).isEqualTo(user.getUserId());
    }
}
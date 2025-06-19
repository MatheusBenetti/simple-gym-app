package com.totex.simplegymapp.infrastructure.repository;

import com.totex.simplegymapp.infrastructure.model.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // When
        UserModel savedUser = userRepository.save(user);
        Optional<UserModel> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("hashedpassword")
                .build();
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldDeleteUserByEmail() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .email("delete@example.com")
                .password("hashedpassword")
                .build();
        userRepository.save(user);

        // When
        userRepository.deleteByEmail("delete@example.com");

        // Then
        Optional<UserModel> foundUser = userRepository.findByEmail("delete@example.com");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // When
        Optional<UserModel> foundUser = userRepository.findByEmail("notfound@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }
}
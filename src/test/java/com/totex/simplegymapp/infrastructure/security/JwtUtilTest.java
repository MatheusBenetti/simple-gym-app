package com.totex.simplegymapp.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "test-secret-key-for-junit-tests-minimum-256-bits-long";
    private final long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", testExpiration);
    }

    @Test
    void shouldGenerateValidToken() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void shouldExtractEmailFromToken() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        String extractedEmail = jwtUtil.extractEmailToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(username);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        boolean isValid = jwtUtil.validateToken(token, username);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        String username = "test@example.com";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken, username);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseForTokenWithWrongUsername() {
        // Given
        String username1 = "user1@example.com";
        String username2 = "user2@example.com";
        String token = jwtUtil.generateToken(username1);

        // When
        boolean isValid = jwtUtil.validateToken(token, username2);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldExtractIssuedAtDate() {
        // Given
        String username = "test@example.com";
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(username);
        Date afterGeneration = new Date();

        // When
        Date issuedAt = jwtUtil.getIssuedAtDateFromToken(token);

        // Then
        assertThat(issuedAt).isBetween(beforeGeneration, afterGeneration);
    }

    @Test
    void shouldExtractExpirationDate() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        // Then
        Date expectedExpiration = new Date(System.currentTimeMillis() + testExpiration);
        // Allow 1 second tolerance for test execution time
        assertThat(expirationDate).isCloseTo(expectedExpiration, 1000);
    }

    @Test
    void shouldReturnCorrectExpirationTime() {
        // When
        long expirationTime = jwtUtil.getExpirationTime();

        // Then
        assertThat(expirationTime).isEqualTo(testExpiration);
    }

    @Test
    void shouldDetectNonExpiredToken() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }
}
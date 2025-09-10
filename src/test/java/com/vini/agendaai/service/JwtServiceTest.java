package com.vini.agendaai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "mySecretKeyForJWTTokenGenerationThatShouldBeLongEnoughAndSecure");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationInMs", 86400000); // 24 hours
    }

    @Test
    void generateToken_ValidEmail_ReturnsToken() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(email);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void getUserEmailFromToken_ValidToken_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        String extractedEmail = jwtService.getUserEmailFromToken(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtService.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtService.validateToken(nullToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateAndValidateToken_CompleteFlow_WorksCorrectly() {
        // Given
        String email = "user@example.com";

        // When
        String token = jwtService.generateToken(email);
        boolean isValid = jwtService.validateToken(token);
        String extractedEmail = jwtService.getUserEmailFromToken(token);

        // Then
        assertNotNull(token);
        assertTrue(isValid);
        assertEquals(email, extractedEmail);
    }
}

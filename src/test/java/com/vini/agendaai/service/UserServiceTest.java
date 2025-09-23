package com.vini.agendaai.service;

import com.vini.agendaai.model.User;
import com.vini.agendaai.repository.UserRepository;
import com.vini.agendaai.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .googleId("google123")
                .profilePictureUrl("https://example.com/photo.jpg")
                .isActive(true)
                .build();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "Test User");
        attributes.put("sub", "google123");
        attributes.put("picture", "https://example.com/photo.jpg");

        oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("sub")).thenReturn("google123");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://example.com/photo.jpg");
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertTrue(userDetails instanceof UserPrincipal);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, 
                () -> userService.loadUserByUsername("nonexistent@example.com"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void processOAuth2User_NewUser_CreatesAndReturnsUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.processOAuth2User(oAuth2User);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("google123", result.getGoogleId());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void processOAuth2User_ExistingUser_UpdatesAndReturnsUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.processOAuth2User(oAuth2User);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(testUser);
    }

    @Test
    void findByEmail_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void updateUserTokens_ValidUser_UpdatesTokens() {
        // Given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(1);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUserTokens(testUser, accessToken, refreshToken, tokenExpiry);

        // Then
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void existsByEmail_UserExists_ReturnsTrue() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void existsByEmail_UserNotExists_ReturnsFalse() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }
}

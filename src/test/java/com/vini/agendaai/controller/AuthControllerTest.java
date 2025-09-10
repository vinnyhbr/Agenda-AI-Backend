package com.vini.agendaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vini.agendaai.model.User;
import com.vini.agendaai.security.UserPrincipal;
import com.vini.agendaai.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .profilePictureUrl("https://example.com/photo.jpg")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    void getCurrentUser_AuthenticatedUser_ReturnsUserDto() throws Exception {
        // Given
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsException() throws Exception {
        // Given
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void logout_ValidRequest_ReturnsSuccessMessage() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout realizado com sucesso"));
    }

    @Test
    void getAuthStatus_AuthenticatedUser_ReturnsAuthenticatedStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/status")
                .with(user(userPrincipal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"));
    }

    @Test
    void getAuthStatus_UnauthenticatedUser_ReturnsUnauthenticatedStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    @WithMockUser
    void getCurrentUser_WithoutUserPrincipal_ThrowsException() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}

package com.vini.agendaai.controller;

import com.vini.agendaai.dto.UserDto;
import com.vini.agendaai.model.User;
import com.vini.agendaai.security.UserPrincipal;
import com.vini.agendaai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> status = new HashMap<>();
        
        if (userPrincipal != null) {
            status.put("authenticated", true);
            status.put("user", Map.of(
                "id", userPrincipal.getId(),
                "email", userPrincipal.getEmail(),
                "name", userPrincipal.getName()
            ));
        } else {
            status.put("authenticated", false);
        }
        
        return ResponseEntity.ok(status);
    }
}

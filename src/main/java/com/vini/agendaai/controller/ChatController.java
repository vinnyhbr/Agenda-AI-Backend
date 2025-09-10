package com.vini.agendaai.controller;

import com.vini.agendaai.dto.ChatMessageDto;
import com.vini.agendaai.dto.ChatRequestDto;
import com.vini.agendaai.model.User;
import com.vini.agendaai.security.UserPrincipal;
import com.vini.agendaai.service.ChatService;
import com.vini.agendaai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChatRequestDto request) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessageDto response = chatService.processUserMessage(user, request.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ChatMessageDto>> getChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageDto> history = chatService.getUserChatHistory(user, pageable);
        
        return ResponseEntity.ok(history);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ChatMessageDto>> getRecentMessages(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatMessageDto> recentMessages = chatService.getRecentChatHistory(user);
        return ResponseEntity.ok(recentMessages);
    }

    @DeleteMapping("/history")
    public ResponseEntity<Map<String, String>> clearChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatService.clearChatHistory(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Histórico de chat limpo com sucesso");
        return ResponseEntity.ok(response);
    }
}

package com.vini.agendaai.service;

import com.vini.agendaai.dto.ChatMessageDto;
import com.vini.agendaai.model.ChatMessage;
import com.vini.agendaai.model.User;
import com.vini.agendaai.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final OpenAIService openAIService;
    private final CalendarEventService calendarEventService;

    @Transactional
    public ChatMessageDto processUserMessage(User user, String message) {
        // Salvar mensagem do usuário
        ChatMessage userMessage = ChatMessage.builder()
                .user(user)
                .message(message)
                .type(ChatMessage.MessageType.USER_MESSAGE)
                .status(ChatMessage.MessageStatus.PROCESSING)
                .build();
        
        userMessage = chatMessageRepository.save(userMessage);

        try {
            // Processar com OpenAI
            String aiResponse = openAIService.processCalendarCommand(user, message);
            
            // Atualizar mensagem com resposta
            userMessage.setResponse(aiResponse);
            userMessage.setStatus(ChatMessage.MessageStatus.COMPLETED);
            userMessage = chatMessageRepository.save(userMessage);

            // Criar mensagem de resposta da IA
            ChatMessage aiMessage = ChatMessage.builder()
                    .user(user)
                    .message(aiResponse)
                    .type(ChatMessage.MessageType.AI_RESPONSE)
                    .status(ChatMessage.MessageStatus.COMPLETED)
                    .build();
            
            chatMessageRepository.save(aiMessage);

            return convertToDto(userMessage);

        } catch (Exception e) {
            log.error("Error processing user message", e);
            
            userMessage.setResponse("Desculpe, ocorreu um erro ao processar sua solicitação. Tente novamente.");
            userMessage.setStatus(ChatMessage.MessageStatus.ERROR);
            userMessage = chatMessageRepository.save(userMessage);

            return convertToDto(userMessage);
        }
    }

    public Page<ChatMessageDto> getUserChatHistory(User user, Pageable pageable) {
        Page<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return messages.map(this::convertToDto);
    }

    public List<ChatMessageDto> getRecentChatHistory(User user) {
        List<ChatMessage> messages = chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public void clearChatHistory(User user) {
        List<ChatMessage> userMessages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
        chatMessageRepository.deleteAll(userMessages);
    }

    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .message(message.getMessage())
                .response(message.getResponse())
                .type(message.getType().name())
                .status(message.getStatus().name())
                .actionPerformed(message.getActionPerformed())
                .calendarEventId(message.getCalendarEventId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

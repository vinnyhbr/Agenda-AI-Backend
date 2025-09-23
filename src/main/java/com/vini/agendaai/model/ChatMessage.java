package com.vini.agendaai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "action_performed")
    private String actionPerformed;

    @Column(name = "calendar_event_id")
    private String calendarEventId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum MessageType {
        USER_MESSAGE,
        AI_RESPONSE,
        SYSTEM_MESSAGE
    }

    public enum MessageStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        ERROR
    }
}

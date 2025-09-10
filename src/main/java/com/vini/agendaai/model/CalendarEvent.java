package com.vini.agendaai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "google_event_id", unique = true)
    private String googleEventId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_all_day")
    @Builder.Default
    private Boolean isAllDay = false;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.CONFIRMED;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    @Column(name = "reminder_minutes")
    private Integer reminderMinutes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EventStatus {
        CONFIRMED,
        TENTATIVE,
        CANCELLED
    }
}

package com.vini.agendaai.controller;

import com.vini.agendaai.dto.CalendarEventDto;
import com.vini.agendaai.model.User;
import com.vini.agendaai.security.UserPrincipal;
import com.vini.agendaai.service.CalendarEventService;
import com.vini.agendaai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarEventService calendarEventService;
    private final UserService userService;

    @PostMapping("/events")
    public ResponseEntity<CalendarEventDto> createEvent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CalendarEventDto eventDto) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CalendarEventDto createdEvent = calendarEventService.createEvent(user, eventDto);
        return ResponseEntity.ok(createdEvent);
    }

    @GetMapping("/events")
    public ResponseEntity<Page<CalendarEventDto>> getEvents(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<CalendarEventDto> events = calendarEventService.getUserEvents(user, pageable);
        
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/range")
    public ResponseEntity<List<CalendarEventDto>> getEventsByRange(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CalendarEventDto> events = calendarEventService.getUserEventsBetween(user, startTime, endTime);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<CalendarEventDto> getEvent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long eventId) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CalendarEventDto event = calendarEventService.getEventById(user, eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return ResponseEntity.ok(event);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<CalendarEventDto> updateEvent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long eventId,
            @Valid @RequestBody CalendarEventDto eventDto) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CalendarEventDto updatedEvent = calendarEventService.updateEvent(user, eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long eventId) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        calendarEventService.deleteEvent(user, eventId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Evento deletado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<List<CalendarEventDto>> syncWithGoogleCalendar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        User user = userService.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Valores padrão: próximos 30 dias
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (endTime == null) {
            endTime = startTime.plusDays(30);
        }

        List<CalendarEventDto> syncedEvents = calendarEventService.syncWithGoogleCalendar(user, startTime, endTime);
        return ResponseEntity.ok(syncedEvents);
    }
}

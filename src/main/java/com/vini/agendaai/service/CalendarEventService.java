package com.vini.agendaai.service;

import com.vini.agendaai.dto.CalendarEventDto;
import com.vini.agendaai.model.CalendarEvent;
import com.vini.agendaai.model.User;
import com.vini.agendaai.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final GoogleCalendarService googleCalendarService;

    @Transactional
    public CalendarEventDto createEvent(User user, CalendarEventDto eventDto) {
        try {
            // Criar evento no Google Calendar primeiro
            CalendarEventDto googleEvent = googleCalendarService.createEvent(user, eventDto);
            
            // Salvar no banco local
            CalendarEvent localEvent = CalendarEvent.builder()
                    .user(user)
                    .googleEventId(googleEvent.getGoogleEventId())
                    .title(eventDto.getTitle())
                    .description(eventDto.getDescription())
                    .startTime(eventDto.getStartTime())
                    .endTime(eventDto.getEndTime())
                    .isAllDay(eventDto.isAllDay())
                    .location(eventDto.getLocation())
                    .reminderMinutes(eventDto.getReminderMinutes())
                    .build();

            localEvent = calendarEventRepository.save(localEvent);
            return convertToDto(localEvent);

        } catch (Exception e) {
            log.error("Error creating calendar event", e);
            throw new RuntimeException("Failed to create calendar event", e);
        }
    }

    @Transactional
    public CalendarEventDto updateEvent(User user, Long eventId, CalendarEventDto eventDto) {
        CalendarEvent existingEvent = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!existingEvent.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this event");
        }

        try {
            // Atualizar no Google Calendar
            googleCalendarService.updateEvent(user, existingEvent.getGoogleEventId(), eventDto);
            
            // Atualizar no banco local
            existingEvent.setTitle(eventDto.getTitle());
            existingEvent.setDescription(eventDto.getDescription());
            existingEvent.setStartTime(eventDto.getStartTime());
            existingEvent.setEndTime(eventDto.getEndTime());
            existingEvent.setIsAllDay(eventDto.isAllDay());
            existingEvent.setLocation(eventDto.getLocation());
            existingEvent.setReminderMinutes(eventDto.getReminderMinutes());

            existingEvent = calendarEventRepository.save(existingEvent);
            return convertToDto(existingEvent);

        } catch (Exception e) {
            log.error("Error updating calendar event", e);
            throw new RuntimeException("Failed to update calendar event", e);
        }
    }

    @Transactional
    public void deleteEvent(User user, Long eventId) {
        CalendarEvent existingEvent = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!existingEvent.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this event");
        }

        try {
            // Deletar do Google Calendar
            googleCalendarService.deleteEvent(user, existingEvent.getGoogleEventId());
            
            // Deletar do banco local
            calendarEventRepository.delete(existingEvent);

        } catch (Exception e) {
            log.error("Error deleting calendar event", e);
            throw new RuntimeException("Failed to delete calendar event", e);
        }
    }

    public Page<CalendarEventDto> getUserEvents(User user, Pageable pageable) {
        Page<CalendarEvent> events = calendarEventRepository.findByUserOrderByStartTimeDesc(user, pageable);
        return events.map(this::convertToDto);
    }

    public List<CalendarEventDto> getUserEventsBetween(User user, LocalDateTime startTime, LocalDateTime endTime) {
        List<CalendarEvent> events = calendarEventRepository
                .findByUserAndStartTimeBetweenOrderByStartTime(user, startTime, endTime);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Optional<CalendarEventDto> getEventById(User user, Long eventId) {
        return calendarEventRepository.findById(eventId)
                .filter(event -> event.getUser().getId().equals(user.getId()))
                .map(this::convertToDto);
    }

    public List<CalendarEventDto> syncWithGoogleCalendar(User user, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // Buscar eventos do Google Calendar
            List<CalendarEventDto> googleEvents = googleCalendarService.getEvents(user, startTime, endTime);
            
            // Sincronizar com banco local (implementação básica)
            for (CalendarEventDto googleEvent : googleEvents) {
                Optional<CalendarEvent> existingEvent = calendarEventRepository
                        .findByGoogleEventId(googleEvent.getGoogleEventId());
                
                if (existingEvent.isEmpty()) {
                    // Criar novo evento local
                    CalendarEvent newEvent = CalendarEvent.builder()
                            .user(user)
                            .googleEventId(googleEvent.getGoogleEventId())
                            .title(googleEvent.getTitle())
                            .description(googleEvent.getDescription())
                            .startTime(googleEvent.getStartTime())
                            .endTime(googleEvent.getEndTime())
                            .isAllDay(googleEvent.isAllDay())
                            .location(googleEvent.getLocation())
                            .build();
                    calendarEventRepository.save(newEvent);
                }
            }
            
            return googleEvents;

        } catch (Exception e) {
            log.error("Error syncing with Google Calendar", e);
            throw new RuntimeException("Failed to sync with Google Calendar", e);
        }
    }

    @Transactional
    public CalendarEvent createEventFromChat(User user, String title, LocalDateTime dateTime) {
        CalendarEventDto eventDto = CalendarEventDto.builder()
                .title(title)
                .startTime(dateTime)
                .endTime(dateTime.plusHours(1)) // padrão: 1h de duração
                .isAllDay(false)
                .build();
        CalendarEventDto created = createEvent(user, eventDto);
        // Buscar entidade persistida para obter o ID
        return calendarEventRepository.findById(created.getId()).orElseThrow();
    }

    private CalendarEventDto convertToDto(CalendarEvent event) {
        return CalendarEventDto.builder()
                .id(event.getId())
                .googleEventId(event.getGoogleEventId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .allDay(event.getIsAllDay())
                .location(event.getLocation())
                .status(event.getStatus().name())
                .recurrenceRule(event.getRecurrenceRule())
                .reminderMinutes(event.getReminderMinutes())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}

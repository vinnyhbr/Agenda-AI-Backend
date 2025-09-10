package com.vini.agendaai.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.vini.agendaai.dto.CalendarEventDto;
import com.vini.agendaai.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Agenda AI";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Calendar getCalendarService(User user) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(user.getAccessToken());

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<CalendarEventDto> getEvents(User user, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Calendar service = getCalendarService(user);
            
            DateTime timeMin = new DateTime(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            DateTime timeMax = new DateTime(endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

            Events events = service.events().list("primary")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching events from Google Calendar", e);
            throw new RuntimeException("Failed to fetch events from Google Calendar", e);
        }
    }

    public CalendarEventDto createEvent(User user, CalendarEventDto eventDto) {
        try {
            Calendar service = getCalendarService(user);
            
            Event event = convertToGoogleEvent(eventDto);
            event = service.events().insert("primary", event).execute();
            
            return convertToDto(event);

        } catch (Exception e) {
            log.error("Error creating event in Google Calendar", e);
            throw new RuntimeException("Failed to create event in Google Calendar", e);
        }
    }

    public CalendarEventDto updateEvent(User user, String eventId, CalendarEventDto eventDto) {
        try {
            Calendar service = getCalendarService(user);
            
            Event event = convertToGoogleEvent(eventDto);
            event = service.events().update("primary", eventId, event).execute();
            
            return convertToDto(event);

        } catch (Exception e) {
            log.error("Error updating event in Google Calendar", e);
            throw new RuntimeException("Failed to update event in Google Calendar", e);
        }
    }

    public void deleteEvent(User user, String eventId) {
        try {
            Calendar service = getCalendarService(user);
            service.events().delete("primary", eventId).execute();

        } catch (Exception e) {
            log.error("Error deleting event from Google Calendar", e);
            throw new RuntimeException("Failed to delete event from Google Calendar", e);
        }
    }

    private CalendarEventDto convertToDto(Event event) {
        CalendarEventDto dto = new CalendarEventDto();
        dto.setGoogleEventId(event.getId());
        dto.setTitle(event.getSummary());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        
        if (event.getStart().getDateTime() != null) {
            dto.setStartTime(LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(event.getStart().getDateTime().getValue()),
                ZoneId.systemDefault()));
        }
        
        if (event.getEnd().getDateTime() != null) {
            dto.setEndTime(LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()),
                ZoneId.systemDefault()));
        }
        
        dto.setAllDay(event.getStart().getDate() != null);
        
        return dto;
    }

    private Event convertToGoogleEvent(CalendarEventDto dto) {
        Event event = new Event()
                .setSummary(dto.getTitle())
                .setDescription(dto.getDescription())
                .setLocation(dto.getLocation());

        if (dto.isAllDay()) {
            event.setStart(new EventDateTime().setDate(
                new DateTime(dto.getStartTime().toLocalDate().toString())));
            event.setEnd(new EventDateTime().setDate(
                new DateTime(dto.getEndTime().toLocalDate().toString())));
        } else {
            event.setStart(new EventDateTime().setDateTime(
                new DateTime(dto.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())));
            event.setEnd(new EventDateTime().setDateTime(
                new DateTime(dto.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())));
        }

        if (dto.getReminderMinutes() != null && dto.getReminderMinutes() > 0) {
            EventReminder reminder = new EventReminder()
                    .setMethod("popup")
                    .setMinutes(dto.getReminderMinutes());
            event.setReminders(new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminder)));
        }

        return event;
    }
}

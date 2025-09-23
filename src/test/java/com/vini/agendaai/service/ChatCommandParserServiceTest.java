package com.vini.agendaai.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChatCommandParserServiceTest {
    private final ChatCommandParserService parser = new ChatCommandParserService();

    @Test
    void parseCreateEvent_AgendaAmanha15h() {
        String msg = "Agende uma reunião amanhã às 15h";
        Optional<ChatCommandParserService.CreateEventCommand> result = parser.parseCreateEvent(msg);
        assertTrue(result.isPresent());
        assertEquals("reunião", result.get().title);
        assertEquals(15, result.get().dateTime.getHour());
    }

    @Test
    void parseCreateEvent_AgendaHoje9h() {
        String msg = "Agende um compromisso hoje às 9h";
        Optional<ChatCommandParserService.CreateEventCommand> result = parser.parseCreateEvent(msg);
        assertTrue(result.isPresent());
        assertEquals("compromisso", result.get().title);
        assertEquals(9, result.get().dateTime.getHour());
    }

    @Test
    void parseCreateEvent_ComandoInvalido() {
        String msg = "Me lembre de beber água";
        Optional<ChatCommandParserService.CreateEventCommand> result = parser.parseCreateEvent(msg);
        assertFalse(result.isPresent());
    }
}


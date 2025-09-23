package com.vini.agendaai.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.vini.agendaai.dto.CalendarEventDto;
import com.vini.agendaai.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class OpenAIService {

    private final OpenAiService openAiService;
    private final CalendarEventService calendarEventService;

    @Value("${app.openai.model:gpt-3.5-turbo}")
    private String model;

    public OpenAIService(@Value("${app.openai.api-key}") String apiKey,
                        CalendarEventService calendarEventService) {
        this.openAiService = new OpenAiService(apiKey);
        this.calendarEventService = calendarEventService;
    }

    public String processCalendarCommand(User user, String userMessage) {
        try {
            // Criar prompt para análise da intenção do usuário
            String systemPrompt = createSystemPrompt();
            String analysisPrompt = createAnalysisPrompt(userMessage);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(Arrays.asList(
                            new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                            new ChatMessage(ChatMessageRole.USER.value(), analysisPrompt)
                    ))
                    .maxTokens(500)
                    .temperature(0.7)
                    .build();

            String aiResponse = openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage().getContent();

            // Processar a resposta da IA e executar ações
            return processAIResponse(user, userMessage, aiResponse);

        } catch (Exception e) {
            log.error("Error processing OpenAI request", e);
            return "Desculpe, não consegui processar sua solicitação no momento. Tente novamente.";
        }
    }

    private String createSystemPrompt() {
        return """
                Você é um assistente de agenda inteligente. Sua função é ajudar o usuário a gerenciar seus eventos de calendário.
                
                Você pode realizar as seguintes ações:
                1. CREATE_EVENT - Criar um novo evento
                2. UPDATE_EVENT - Atualizar um evento existente
                3. DELETE_EVENT - Deletar um evento
                4. LIST_EVENTS - Listar eventos
                5. SEARCH_EVENTS - Buscar eventos específicos
                
                Para cada solicitação do usuário, você deve:
                1. Identificar a intenção (CREATE_EVENT, UPDATE_EVENT, DELETE_EVENT, LIST_EVENTS, SEARCH_EVENTS)
                2. Extrair informações relevantes (título, data, hora, descrição, local)
                3. Responder de forma natural e amigável
                
                Formato de resposta:
                ACTION: [ação identificada]
                TITLE: [título do evento, se aplicável]
                START_TIME: [data e hora de início no formato yyyy-MM-dd HH:mm, se aplicável]
                END_TIME: [data e hora de fim no formato yyyy-MM-dd HH:mm, se aplicável]
                DESCRIPTION: [descrição, se aplicável]
                LOCATION: [local, se aplicável]
                RESPONSE: [resposta natural para o usuário]
                """;
    }

    private String createAnalysisPrompt(String userMessage) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("""
                Analise a seguinte mensagem do usuário e identifique a intenção e extraia as informações relevantes:
                
                Mensagem: "%s"
                
                Data/hora atual: %s
                
                Considere que quando o usuário mencionar "hoje", "amanhã", "próxima semana", etc., 
                calcule as datas baseado na data atual fornecida.
                """, userMessage, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    private String processAIResponse(User user, String originalMessage, String aiResponse) {
        try {
            String action = extractValue(aiResponse, "ACTION");
            String response = extractValue(aiResponse, "RESPONSE");

            switch (action.toUpperCase()) {
                case "CREATE_EVENT":
                    return handleCreateEvent(user, aiResponse, response);
                case "LIST_EVENTS":
                    return handleListEvents(user, response);
                case "SEARCH_EVENTS":
                    return handleSearchEvents(user, originalMessage, response);
                default:
                    return response != null ? response : 
                           "Entendi sua solicitação, mas ainda não posso executar essa ação específica.";
            }

        } catch (Exception e) {
            log.error("Error processing AI response", e);
            return "Processamento concluído, mas ocorreu um erro na execução.";
        }
    }

    private String handleCreateEvent(User user, String aiResponse, String response) {
        try {
            String title = extractValue(aiResponse, "TITLE");
            String startTimeStr = extractValue(aiResponse, "START_TIME");
            String endTimeStr = extractValue(aiResponse, "END_TIME");
            String description = extractValue(aiResponse, "DESCRIPTION");
            String location = extractValue(aiResponse, "LOCATION");

            if (title == null || startTimeStr == null) {
                return "Para criar um evento, preciso pelo menos do título e data/hora. " + response;
            }

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endTime = endTimeStr != null ? 
                LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
                startTime.plusHours(1);

            CalendarEventDto eventDto = CalendarEventDto.builder()
                    .title(title)
                    .description(description)
                    .startTime(startTime)
                    .endTime(endTime)
                    .location(location)
                    .build();

            CalendarEventDto createdEvent = calendarEventService.createEvent(user, eventDto);
            
            return String.format("✅ Evento criado com sucesso!\n\n" +
                    "📅 %s\n" +
                    "🕐 %s às %s\n" +
                    "%s%s",
                    createdEvent.getTitle(),
                    createdEvent.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    createdEvent.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    createdEvent.getLocation() != null ? "📍 " + createdEvent.getLocation() + "\n" : "",
                    createdEvent.getDescription() != null ? "📝 " + createdEvent.getDescription() : "");

        } catch (Exception e) {
            log.error("Error creating event", e);
            return "Não consegui criar o evento. Verifique se as informações estão corretas. " + response;
        }
    }

    private String handleListEvents(User user, String response) {
        try {
            LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0);
            LocalDateTime endTime = startTime.plusDays(7);
            
            List<CalendarEventDto> events = calendarEventService.getUserEventsBetween(user, startTime, endTime);
            
            if (events.isEmpty()) {
                return "📅 Você não tem eventos programados para os próximos 7 dias.";
            }

            StringBuilder eventsList = new StringBuilder("📅 Seus próximos eventos:\n\n");
            for (CalendarEventDto event : events) {
                eventsList.append(String.format("• %s\n  🕐 %s às %s\n",
                        event.getTitle(),
                        event.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
                
                if (event.getLocation() != null) {
                    eventsList.append("  📍 ").append(event.getLocation()).append("\n");
                }
                eventsList.append("\n");
            }

            return eventsList.toString();

        } catch (Exception e) {
            log.error("Error listing events", e);
            return "Não consegui listar os eventos no momento. " + response;
        }
    }

    private String handleSearchEvents(User user, String originalMessage, String response) {
        // Implementação básica de busca
        return "🔍 Funcionalidade de busca em desenvolvimento. " + response;
    }

    private String extractValue(String text, String key) {
        Pattern pattern = Pattern.compile(key + ":\\s*(.+?)(?=\\n[A-Z_]+:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}

package com.restaurant.mcpclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpChatService {

    private static final int MAX_MEMORY_INTERACTIONS = 6;
    private static final int MAX_MEMORY_CHARS = 2_000;
    private static final int MAX_MEMORY_CONVERSATIONS = 500;

    private final ChatClient chatClient;
    private final Clock clock;

    @Value("${spring.ai.mcp.client.streamable-http.connections.restaurant-server.url}")
    private String mcpServerUrl;

    @Value("${spring.ai.mcp.client.streamable-http.connections.restaurant-server.endpoint:/mcp}")
    private String mcpServerEndpoint;

    private final Map<String, ConversationMemory> memoryByConversation = new ConcurrentHashMap<>();

    public String chat(String message) {
        return chat("local-chat", message, null);
    }

    public String chat(String conversationId, String message, String senderPhone) {
        ConversationMemory conversationMemory = memoryByConversation.computeIfAbsent(conversationId, ignored -> new ConversationMemory());
        conversationMemory.touch();
        pruneConversationCache();

        Deque<String> memory = conversationMemory.interactions();
        String memoryContext = buildMemoryContext(memory);
        String today = LocalDate.now(clock).toString();
        String phoneInstruction = StringUtils.hasText(senderPhone)
                ? "El teléfono real del cliente es " + senderPhone + ". Si creas una reserva, llama la tool createReservation con exactamente ese teléfono y nunca pidas el teléfono. " +
                "Si cancelas una reserva, llama la tool cancelReservation con requesterPhone exactamente igual a ese teléfono; no aceptes cancelar reservas asociadas a otro número. " +
                "Si el usuario pregunta por sus reservas o quiere cancelar sin recordar el ID, usa getReservationsByPhone con ese teléfono antes de decidir. "
                : "Si creas o cancelas una reserva desde chat local y no hay teléfono, pide solo un teléfono de contacto para autenticar la operación. ";

        log.info("Contexto conversacional para {}: {} interacciones, {} caracteres", conversationId, memory.size(), memoryContext.length());

        try {
            String response = chatClient.prompt()
                    .system("Eres el chatbot de un restaurante. Responde en español de forma amable y breve. " +
                            "Usa las tools MCP remotas para consultar menús y gestionar reservas. " +
                            "Hoy es " + today + ". Interpreta mañana, pasado o este jueves usando esa fecha. " +
                            phoneInstruction +
                            "Contexto reciente del cliente: " + memoryContext)
                    .user(message)
                    .call()
                    .content();

            rememberInteraction(memory, message, response);

            return response;
        } catch (RuntimeException ex) {
            log.error("Error conectando con MCP server en {}{}: {}. " +
                            "Si estás usando Docker Compose, RESTAURANT_MCP_SERVER_URL debe ser http://mcp-server:8080. " +
                            "Usa ngrok solo para exponer mcp-client (/webhooks/twilio), no para la conexión interna MCP.",
                    mcpServerUrl, mcpServerEndpoint, ex.getMessage(), ex);
            return "Ahora mismo no puedo conectar con el servidor de herramientas del restaurante. " +
                    "Revisa la configuración MCP interna y vuelve a intentarlo.";
        }
    }

    private void rememberInteraction(Deque<String> memory, String message, String response) {
        memory.addLast("Usuario: " + message + " | Asistente: " + response);
        trimMemory(memory);
    }

    private String buildMemoryContext(Deque<String> memory) {
        trimMemory(memory);
        return String.join("\n", memory);
    }

    private void trimMemory(Deque<String> memory) {
        while (memory.size() > MAX_MEMORY_INTERACTIONS || totalChars(memory) > MAX_MEMORY_CHARS) {
            memory.pollFirst();
        }
    }

    private int totalChars(Deque<String> memory) {
        return memory.stream().mapToInt(String::length).sum();
    }

    private void pruneConversationCache() {
        int conversationsToRemove = memoryByConversation.size() - MAX_MEMORY_CONVERSATIONS;
        if (conversationsToRemove <= 0) {
            return;
        }

        memoryByConversation.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getValue().lastAccess()))
                .limit(conversationsToRemove)
                .forEach(entry -> memoryByConversation.remove(entry.getKey(), entry.getValue()));
    }

    private static final class ConversationMemory {
        private final Deque<String> interactions = new ConcurrentLinkedDeque<>();
        private volatile long lastAccess = System.currentTimeMillis();

        private Deque<String> interactions() {
            return interactions;
        }

        private long lastAccess() {
            return lastAccess;
        }

        private void touch() {
            lastAccess = System.currentTimeMillis();
        }
    }
}

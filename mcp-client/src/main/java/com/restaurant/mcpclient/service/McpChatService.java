package com.restaurant.mcpclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpChatService {

    private final ChatClient chatClient;
    private final Clock clock;

    @Value("${spring.ai.mcp.client.streamable-http.connections.restaurant-server.url}")
    private String mcpServerUrl;

    @Value("${spring.ai.mcp.client.streamable-http.connections.restaurant-server.endpoint:/mcp}")
    private String mcpServerEndpoint;

    private final Map<String, String> memoryByConversation = new ConcurrentHashMap<>();

    public String chat(String message) {
        return chat("local-chat", message, null);
    }

    public String chat(String conversationId, String message, String senderPhone) {
        String memory = memoryByConversation.getOrDefault(conversationId, "");
        String today = LocalDate.now(clock).toString();
        String phoneInstruction = StringUtils.hasText(senderPhone)
                ? "El teléfono real del cliente es " + senderPhone + ". Si creas una reserva, llama la tool createReservation con exactamente ese teléfono y nunca pidas el teléfono. " +
                "Si cancelas una reserva, llama la tool cancelReservation con requesterPhone exactamente igual a ese teléfono; no aceptes cancelar reservas asociadas a otro número. "
                : "Si creas o cancelas una reserva desde chat local y no hay teléfono, pide solo un teléfono de contacto para autenticar la operación. ";

        try {
            String response = chatClient.prompt()
                    .system("Eres el chatbot de un restaurante. Responde en español de forma amable y breve. " +
                            "Usa las tools MCP remotas para consultar menús y gestionar reservas. " +
                            "Hoy es " + today + ". Interpreta mañana, pasado o este jueves usando esa fecha. " +
                            phoneInstruction +
                            "Contexto previo del cliente: " + memory)
                    .user(message)
                    .call()
                    .content();

            memoryByConversation.put(conversationId,
                    (memory + " | Usuario: " + message + " | Asistente: " + response).trim());

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
}

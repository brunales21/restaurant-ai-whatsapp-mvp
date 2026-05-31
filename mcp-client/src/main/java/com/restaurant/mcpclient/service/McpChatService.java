package com.restaurant.mcpclient.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class McpChatService {

    private final ChatClient chatClient;
    private final Clock clock;

    private final Map<String, String> memoryByConversation = new ConcurrentHashMap<>();

    public String chat(String message) {
        return chat("local-chat", message, null);
    }

    public String chat(String conversationId, String message, String senderPhone) {
        String memory = memoryByConversation.getOrDefault(conversationId, "");
        String today = LocalDate.now(clock).toString();
        String phoneInstruction = StringUtils.hasText(senderPhone)
                ? "El teléfono real del cliente es " + senderPhone + ". Si creas una reserva, llama la tool createReservation con exactamente ese teléfono y nunca pidas el teléfono. "
                : "Si creas una reserva desde chat local y no hay teléfono, pide solo un teléfono de contacto. ";

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
    }
}

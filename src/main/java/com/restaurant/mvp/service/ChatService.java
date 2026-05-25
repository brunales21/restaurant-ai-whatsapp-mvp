package com.restaurant.mvp.service;

import com.restaurant.mvp.ai.tools.RestaurantTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final RestaurantTools restaurantTools;
    private final Clock clock;

    private final Map<String, String> memoryByPhone = new ConcurrentHashMap<>();

    public String chat(String userMessage) {
        return chat("local-chat", userMessage);
    }

    public String chat(String conversationId, String userMessage) {
        String memory = memoryByPhone.getOrDefault(conversationId, "");
        log.info("Memory for {}: {}", conversationId, memory);
        String today = LocalDate.now(clock).toString();

        String response = chatClient.prompt()
                .system("Eres el asistente de un restaurante. Responde en español de forma amable y breve. " +
                        "Usa tools cuando el usuario pregunte por menú o quiera crear/cancelar reservas. " +
                        "Hoy es " + today + ". Interpreta mañana/pasado/este jueves usando esta fecha. " +
                        "Si falta año en la fecha, asume el año actual de hoy. " +
                        "Nunca pidas ni inventes el teléfono para reservar: usa el número del remitente actual. " +
                        "Contexto previo del cliente: " + memory)
                .user(userMessage)
                .tools(restaurantTools)
                .call()
                .content();

        memoryByPhone.put(conversationId,
                (memory + " | Usuario: " + userMessage + " | Asistente: " + response)
                        .trim());

        return response;
    }
}

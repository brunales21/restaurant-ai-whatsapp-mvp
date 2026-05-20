package com.restaurant.mvp.service;

import com.restaurant.mvp.ai.tools.RestaurantTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final RestaurantTools restaurantTools;

    public String chat(String userMessage) {
        return chatClient.prompt()
                .system("Eres el asistente de un restaurante. Responde en español de forma amable y breve. " +
                        "Usa tools cuando el usuario pregunte por menú o quiera crear/cancelar reservas.")
                .user(userMessage)
                .tools(restaurantTools)
                .call()
                .content();
    }
}

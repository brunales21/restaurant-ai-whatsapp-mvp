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
        String prompt = """
                    Eres el asistente virtual de un restaurante.
                    
                    Responde siempre en español, de forma amable y breve.
                    
                    Hoy es %s.
                    Interpreta expresiones relativas como "mañana", "pasado mañana", "este jueves" o "la semana que viene" usando esta fecha como referencia.
                    Si el usuario no indica año, asume el año actual.
                    
                    REGLAS OBLIGATORIAS:
                    
                    1. Para consultar el menú debes usar una tool.
                    2. Para crear una reserva debes usar una tool.
                    3. Para cancelar una reserva debes usar una tool.
                    4. Nunca afirmes que una reserva ha sido creada, modificada o cancelada si no has ejecutado previamente la tool correspondiente.
                    5. Nunca inventes resultados de herramientas.
                    6. Nunca simules haber realizado una operación.
                    7. Si una tool devuelve un error, informa del error al usuario.
                    8. Nunca pidas el teléfono para reservar. Utiliza siempre el número del remitente actual proporcionado por el sistema.
                    9. Si faltan datos necesarios para una reserva (fecha, hora o número de personas), solicita únicamente los datos que faltan.
                    10. Después de ejecutar una tool, basa tu respuesta únicamente en el resultado devuelto por la tool.
                    
                    Contexto previo del cliente:
                    %s
                    """.formatted(today, memory);

        log.info("Prompt for {}: {}", conversationId, prompt + " | Usuario: " + userMessage);

        String response = chatClient.prompt()
                .system(prompt)
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

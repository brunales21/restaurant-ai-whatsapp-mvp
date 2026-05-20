package com.restaurant.mvp.controller;

import com.restaurant.mvp.client.WhatsappClient;
import com.restaurant.mvp.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhooks/whatsapp")
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private final ChatService chatService;
    private final WhatsappClient whatsappClient;

    // Lombok @Slf4j proporciona 'log'

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Forbidden");
    }

    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody Map<String, Object> payload) {
        String from = extractFrom(payload);
        String message = extractMessage(payload);

        if (from != null && message != null && !message.isBlank()) {
            String reply = chatService.chat(message);
            whatsappClient.sendTextMessage(from, reply);
        } else {
            log.info("Webhook recibido sin mensaje de texto procesable");
        }

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private String extractFrom(Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entries.getFirst().get("changes");
            Map<String, Object> value = (Map<String, Object>) changes.getFirst().get("value");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            return (String) messages.getFirst().get("from");
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entries.getFirst().get("changes");
            Map<String, Object> value = (Map<String, Object>) changes.getFirst().get("value");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            Map<String, Object> text = (Map<String, Object>) messages.getFirst().get("text");
            return (String) text.get("body");
        } catch (Exception e) {
            return null;
        }
    }
}

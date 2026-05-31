package com.restaurant.mcpclient.controller;

import com.restaurant.mcpclient.client.TwilioWhatsappClient;
import com.restaurant.mcpclient.service.McpChatService;
import com.twilio.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhooks/twilio")
@RequiredArgsConstructor
public class TwilioWebhookController {

    private final McpChatService mcpChatService;
    private final TwilioWhatsappClient twilioWhatsappClient;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> receiveMessage(
            @RequestParam(name = "From", required = false) String from,
            @RequestParam(name = "Body", required = false) String body) {

        log.info("Mensaje entrante Twilio WhatsApp from={} body={}", from, body);

        if (!StringUtils.hasText(from) || !StringUtils.hasText(body)) {
            log.info("Webhook Twilio recibido sin From/Body válidos");
            return ResponseEntity.ok().build();
        }

        String senderPhone = twilioWhatsappClient.toDigitsOnly(from);
        String response = mcpChatService.chat(from, body, senderPhone);
        log.info("Respuesta generada por IA para {}: {}", from, response);

        try {
            twilioWhatsappClient.sendTextMessage(from, response);
        } catch (ApiException | IllegalArgumentException ex) {
            log.error("Error enviando mensaje a Twilio para {}: {}", from, ex.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}

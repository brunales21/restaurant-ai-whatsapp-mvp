package com.restaurant.mvp.controller;

import com.restaurant.mvp.client.TwilioWhatsappClient;
import com.restaurant.mvp.service.ChatService;
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

    private final ChatService chatService;
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

        String response = chatService.chat(from, body);
        log.info("Respuesta generada por IA para {}: {}", from, response);

        twilioWhatsappClient.sendTextMessage(from, response);

        return ResponseEntity.ok().build();
    }
}

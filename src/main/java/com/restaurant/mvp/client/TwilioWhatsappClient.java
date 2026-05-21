package com.restaurant.mvp.client;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class TwilioWhatsappClient {

    private static final String WHATSAPP_PREFIX = "whatsapp:";

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-number}")
    private String whatsappNumber;

    @PostConstruct
    void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendTextMessage(String to, String text) {
        String normalizedTo = normalizeWhatsappAddress(to);
        String normalizedFrom = normalizeWhatsappAddress(whatsappNumber);

        log.info("Enviando respuesta a Twilio WhatsApp. from={} to={}", normalizedFrom, normalizedTo);
        Message.creator(
                new PhoneNumber(normalizedTo),
                new PhoneNumber(normalizedFrom),
                text
        ).create();
    }

    private String normalizeWhatsappAddress(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Número de WhatsApp vacío o nulo");
        }
        String trimmed = value.trim();
        return trimmed.startsWith(WHATSAPP_PREFIX) ? trimmed : WHATSAPP_PREFIX + trimmed;
    }
}

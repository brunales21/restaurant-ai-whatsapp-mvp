package com.restaurant.mvp.client;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioWhatsappClient {

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
        log.info("Enviando respuesta a Twilio WhatsApp. to={}", to);
        Message.creator(
                new PhoneNumber(to),
                // Twilio WhatsApp requires the from (sender) to be prefixed with "whatsapp:"
                new PhoneNumber("whatsapp:" + whatsappNumber),
                text
        ).create();
    }
}

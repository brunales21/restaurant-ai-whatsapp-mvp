package com.restaurant.mvp.service;

import org.springframework.stereotype.Component;

@Component
public class ConversationContext {

    private static final ThreadLocal<String> CURRENT_PHONE = new ThreadLocal<>();

    public void setCurrentPhone(String phone) {
        CURRENT_PHONE.set(phone);
    }

    public String getCurrentPhone() {
        return CURRENT_PHONE.get();
    }

    public void clear() {
        CURRENT_PHONE.remove();
    }
}

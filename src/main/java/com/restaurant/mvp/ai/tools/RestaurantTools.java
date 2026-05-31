package com.restaurant.mvp.ai.tools;

import com.restaurant.mvp.service.ConversationContext;
import com.restaurant.mvp.service.MenuService;
import com.restaurant.mvp.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class RestaurantTools {

    private final MenuService menuService;
    private final ReservationService reservationService;
    private final ConversationContext conversationContext;

    @Tool(description = "Obtiene el menú del día actual del restaurante")
    public String getTodayMenu() {
        return menuService.getTodayMenu();
    }

    @Tool(description = "Obtiene el menú de una fecha concreta. Usa formato ISO yyyy-MM-dd")
    public String getMenuByDate(LocalDate menuDate) {
        return menuService.getMenuByDate(menuDate);
    }

    @Tool(description = "Crea una reserva de restaurante con nombre, fecha real, hora y número de personas. El teléfono se toma del remitente de WhatsApp.")
    public String createReservation(String customerName, String phone, LocalDate reservationDate, LocalTime reservationTime, Integer people) {
        String effectivePhone = conversationContext.getCurrentPhone();
        log.info("numero de telefono: {}" , effectivePhone);
        return reservationService.createReservation(customerName, effectivePhone, reservationDate, reservationTime, people);
    }

    @Tool(description = "Cancela una reserva por ID o por teléfono")
    public String cancelReservation(Long reservationId, String phone) {
        String effectivePhone = conversationContext.getCurrentPhone();
        return reservationService.cancelReservation(reservationId, effectivePhone);
    }
}

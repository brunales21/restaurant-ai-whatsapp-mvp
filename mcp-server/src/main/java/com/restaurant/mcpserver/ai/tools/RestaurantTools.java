package com.restaurant.mcpserver.ai.tools;

import com.restaurant.mcpserver.service.MenuService;
import com.restaurant.mcpserver.service.ReservationService;
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

    @Tool(description = "Obtiene el menú del día actual del restaurante")
    public String getTodayMenu() {
        return menuService.getTodayMenu();
    }

    @Tool(description = "Obtiene el menú de una fecha concreta. Usa formato ISO yyyy-MM-dd")
    public String getMenuByDate(LocalDate menuDate) {
        return menuService.getMenuByDate(menuDate);
    }

    @Tool(description = "Crea una reserva con nombre, teléfono normalizado, fecha real, hora y número de personas")
    public String createReservation(String customerName, String phone, LocalDate reservationDate, LocalTime reservationTime, Integer people) {
        return reservationService.createReservation(customerName, phone, reservationDate, reservationTime, people);
    }

    @Tool(description = "Consulta reservas activas por teléfono normalizado. Úsala para recuperar reservas reales antes de cancelar o cuando el cliente pregunte por sus reservas.")
    public String getReservationsByPhone(String phone) {
        return reservationService.getReservationsByPhone(phone);
    }

    @Tool(description = "Cancela una reserva solo si el teléfono del solicitante coincide con el teléfono asociado a la reserva. Requiere el teléfono real del remitente en requesterPhone.")
    public String cancelReservation(Long reservationId, String requesterPhone) {
        return reservationService.cancelReservation(reservationId, requesterPhone);
    }
}

package com.restaurant.mcpserver.ai.tools;

import com.restaurant.mcpserver.service.MenuService;
import com.restaurant.mcpserver.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantTools {

    private final MenuService menuService;
    private final ReservationService reservationService;

    @Tool(description = "Obtiene el menú del día actual del restaurante")
    public String getTodayMenu() {
        log.info("Tool called: getTodayMenu");
        return menuService.getTodayMenu();
    }

    @Tool(description = "Obtiene el menú de una fecha concreta. Usa formato ISO yyyy-MM-dd")
    public String getMenuByDate(LocalDate menuDate) {
        log.info("Tool called: getMenuByDate - menuDate={}", menuDate);
        return menuService.getMenuByDate(menuDate);
    }

    @Tool(description = "Crea una reserva con nombre, teléfono normalizado, fecha real, hora y número de personas")
    public String createReservation(
            String customerName,
            String phone,
            LocalDate reservationDate,
            LocalTime reservationTime,
            Integer people) {

        log.info(
                "Tool called: createReservation - customerName={}, phone={}, reservationDate={}, reservationTime={}, people={}",
                customerName,
                phone,
                reservationDate,
                reservationTime,
                people);

        return reservationService.createReservation(
                customerName,
                phone,
                reservationDate,
                reservationTime,
                people);
    }

    @Tool(description = "Cancela una reserva por ID o por teléfono")
    public String cancelReservation(Long reservationId, String phone) {
        log.info(
                "Tool called: cancelReservation - reservationId={}, phone={}",
                reservationId,
                phone);

        return reservationService.cancelReservation(reservationId, phone);
    }
}
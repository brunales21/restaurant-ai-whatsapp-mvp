package com.restaurant.mvp.service;

import com.restaurant.mvp.entity.DailyMenu;
import com.restaurant.mvp.repository.DailyMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final DailyMenuRepository dailyMenuRepository;

    public String getTodayMenu() {
        LocalDate today = LocalDate.now();
        return dailyMenuRepository.findByMenuDate(today)
                .map(this::formatMenu)
                .orElse("Hoy no hay menú publicado todavía.");
    }

    private String formatMenu(DailyMenu menu) {
        return String.format("Menú de hoy (%s): entrante %s, principal %s, postre %s. Precio: %.2f €",
                menu.getMenuDate(),
                menu.getStarter(),
                menu.getMainCourse(),
                menu.getDessert(),
                menu.getPrice());
    }
}

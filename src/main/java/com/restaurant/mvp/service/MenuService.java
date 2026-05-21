package com.restaurant.mvp.service;

import com.restaurant.mvp.entity.DailyMenu;
import com.restaurant.mvp.repository.DailyMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final DailyMenuRepository dailyMenuRepository;
    private final Clock clock;

    public String getTodayMenu() {
        return getMenuByDate(LocalDate.now(clock));
    }

    public String getMenuByDate(LocalDate date) {
        return dailyMenuRepository.findByMenuDate(date)
                .map(this::formatMenu)
                .orElse("No hay menú publicado para el " + date + ".");
    }

    private String formatMenu(DailyMenu menu) {
        return String.format("Menú del %s: entrante %s, principal %s, postre %s. Precio: %.2f €",
                menu.getMenuDate(),
                menu.getStarter(),
                menu.getMainCourse(),
                menu.getDessert(),
                menu.getPrice());
    }
}

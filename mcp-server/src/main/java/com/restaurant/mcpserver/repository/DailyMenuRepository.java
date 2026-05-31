package com.restaurant.mcpserver.repository;

import com.restaurant.mcpserver.entity.DailyMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {
    Optional<DailyMenu> findByMenuDate(LocalDate menuDate);
}

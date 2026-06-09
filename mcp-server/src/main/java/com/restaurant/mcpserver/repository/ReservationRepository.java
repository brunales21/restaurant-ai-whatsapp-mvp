package com.restaurant.mcpserver.repository;

import com.restaurant.mcpserver.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findFirstByPhoneAndStatus(String phone, String status);
}

package com.restaurant.mvp.service;

import com.restaurant.mvp.entity.Reservation;
import com.restaurant.mvp.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String CONFIRMED = "CONFIRMED";
    private static final String CANCELED = "CANCELED";

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional
    public String createReservation(String customerName, String phone, LocalDate date, LocalTime time, Integer people) {
        if (date == null || time == null || people == null || people <= 0) {
            return "Faltan datos para reservar: fecha, hora y número de personas son obligatorios.";
        }
        if (date.isBefore(LocalDate.now(clock))) {
            return "No puedo crear reservas en fechas pasadas. Indica una fecha actual o futura.";
        }

        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            return "No pude detectar un teléfono válido desde el remitente de WhatsApp.";
        }

        Reservation reservation = Reservation.builder()
                .customerName(customerName)
                .phone(normalizedPhone)
                .reservationDate(date)
                .reservationTime(time)
                .people(people)
                .status(CONFIRMED)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return "Reserva creada con éxito. ID de reserva: " + saved.getId() + ". Teléfono asociado: " + saved.getPhone() +
                ". Para " + saved.getCustomerName() + " el " + saved.getReservationDate() + " a las " +
                saved.getReservationTime() + " para " + saved.getPeople() + " personas.";
    }

    @Transactional
    public String cancelReservation(Long reservationId, String phone) {
        if (reservationId != null) {
            return reservationRepository.findById(reservationId)
                    .map(this::cancelAndBuildMessage)
                    .orElse("No encontré una reserva con ese ID.");
        }

        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone != null) {
            return reservationRepository.findFirstByPhoneAndStatus(normalizedPhone, CONFIRMED)
                    .map(this::cancelAndBuildMessage)
                    .orElse("No encontré una reserva activa para ese teléfono.");
        }

        return "Para cancelar, necesito un ID de reserva o un teléfono.";
    }

    private String cancelAndBuildMessage(Reservation reservation) {
        reservation.setStatus(CANCELED);
        return "Reserva cancelada. ID " + reservation.getId() + " para " + reservation.getCustomerName() + ".";
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return "phone null";
        }
        String withoutChannel = rawPhone.replace("whatsapp:", "").trim();
        String digitsOnly = withoutChannel.replaceAll("[^0-9]", "");
        return digitsOnly.isBlank() ? null : digitsOnly;
    }
}

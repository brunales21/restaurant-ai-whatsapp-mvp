package com.restaurant.mcpserver.service;

import com.restaurant.mcpserver.entity.Reservation;
import com.restaurant.mcpserver.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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


    @Transactional(readOnly = true)
    public String getReservationsByPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            return "Necesito un teléfono válido para consultar reservas.";
        }

        List<Reservation> reservations = reservationRepository
                .findByPhoneAndStatusOrderByReservationDateAscReservationTimeAsc(normalizedPhone, CONFIRMED);

        if (reservations.isEmpty()) {
            return "No encontré reservas activas asociadas a ese teléfono.";
        }

        return "Reservas activas asociadas al teléfono " + normalizedPhone + ":\n" + reservations.stream()
                .map(this::formatReservationSummary)
                .collect(Collectors.joining("\n"));
    }

    @Transactional
    public String cancelReservation(Long reservationId, String phone) {
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone == null) {
            return "Por seguridad, solo puedo cancelar reservas usando el teléfono asociado al remitente.";
        }

        if (reservationId != null) {
            return reservationRepository.findById(reservationId)
                    .map(reservation -> cancelIfOwnedByPhone(reservation, normalizedPhone))
                    .orElse("No encontré una reserva con ese ID.");
        }

        return reservationRepository.findFirstByPhoneAndStatus(normalizedPhone, CONFIRMED)
                .map(this::cancelAndBuildMessage)
                .orElse("No encontré una reserva activa asociada a tu teléfono.");
    }

    private String formatReservationSummary(Reservation reservation) {
        return "ID " + reservation.getId() + " - " + reservation.getCustomerName() + " - " +
                reservation.getReservationDate() + " a las " + reservation.getReservationTime() +
                " - " + reservation.getPeople() + " personas";
    }

    private String cancelIfOwnedByPhone(Reservation reservation, String normalizedPhone) {
        String reservationPhone = normalizePhone(reservation.getPhone());
        if (!CONFIRMED.equals(reservation.getStatus())) {
            return "La reserva indicada no está activa.";
        }
        if (!normalizedPhone.equals(reservationPhone)) {
            return "No puedo cancelar esta reserva porque no está asociada a tu teléfono.";
        }
        return cancelAndBuildMessage(reservation);
    }

    private String cancelAndBuildMessage(Reservation reservation) {
        reservation.setStatus(CANCELED);
        return "Reserva cancelada. ID " + reservation.getId() + " para " + reservation.getCustomerName() + ".";
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return null;
        }
        String withoutChannel = rawPhone.replace("whatsapp:", "").trim();
        String digitsOnly = withoutChannel.replaceAll("[^0-9]", "");
        return digitsOnly.isBlank() ? null : digitsOnly;
    }
}

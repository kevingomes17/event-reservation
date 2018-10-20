package com.twa.evtreg.models.dto;

import com.twa.evtreg.models.entities.Reservation;

import java.util.Date;

public class ReservationRes extends Reservation {

    public ReservationRes() {
        super();
    }

    public ReservationRes(Reservation reservation) {
        super(reservation.getId(), reservation.getEmail(), reservation.getName(),
                reservation.getArrivalDate(), reservation.getDepartureDate());
    }

    public ReservationRes(Long id, String email, String name, Date arrivalDate, Date departureDate) {
        super(id, email, name, arrivalDate, departureDate);
    }
}

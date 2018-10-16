package com.twa.evtreg.models.dto;

import com.twa.evtreg.models.Reservation;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class ReservationReq extends Reservation {
    public ReservationReq() {
        super();
    }

    public ReservationReq(Long id, String email, String name, Date arrivalDate, Date departureDate) {
        super(id, email, name, arrivalDate, departureDate);
    }
}

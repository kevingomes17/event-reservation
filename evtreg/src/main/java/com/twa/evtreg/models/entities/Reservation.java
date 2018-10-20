package com.twa.evtreg.models.entities;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Email must not be blank!")
    private String email;

    @NotBlank(message = "Name must not be blank!")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Arrival Date must not be blank!")
    @Temporal(TemporalType.TIMESTAMP)
    private Date arrivalDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Departure Date must not be blank!")
    @Temporal(TemporalType.TIMESTAMP)
    private Date departureDate;

    public Reservation() {}

    public Reservation(Long id, String email, String name, Date arrivalDate, Date departureDate) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

    @Override
    public boolean equals(Object obj) {
        Reservation anotherReservation = (Reservation) obj;
        return this.id.equals(anotherReservation.id) && this.name.equals(anotherReservation.name) && this.email.equals(anotherReservation.email) && this.areReservationDatesEqual(anotherReservation);
    }

    public boolean areReservationDatesEqual(Reservation anotherReservation) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormatter.format(this.arrivalDate).equals(dateFormatter.format(anotherReservation.arrivalDate)) &&
                dateFormatter.format(this.departureDate).equals(dateFormatter.format(anotherReservation.departureDate));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }
}

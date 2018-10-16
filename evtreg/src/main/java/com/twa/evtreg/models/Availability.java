package com.twa.evtreg.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Boolean isAvailable;
    private Date venueDate;

    public Availability() {}

    public Availability(Long id, Date venueDate, Boolean isAvailable) {
        this.id = id;
        this.venueDate = venueDate;
        this.isAvailable = isAvailable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getVenueDate() {
        return venueDate;
    }

    public void setVenueDate(Date venueDate) {
        this.venueDate = venueDate;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}

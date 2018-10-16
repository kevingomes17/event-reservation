package com.twa.evtreg.models;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class AvailabilitySearch {
    @NotNull(message = "Start Date must not be blank!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date start;

    @NotNull(message = "End Date must not be blank!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date end;

    public AvailabilitySearch() {}

    public AvailabilitySearch(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}

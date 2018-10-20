package com.twa.evtreg.models.dto;

import com.twa.evtreg.models.AvailabilitySearch;

import java.util.Date;
import java.util.List;

public class AvailabilitySearchRes extends AvailabilitySearch {
    private List<Date> dates;

    public AvailabilitySearchRes() {
        super();
    }

    public AvailabilitySearchRes(Date start, Date end, List<Date> dates) {
        super(start, end);
        this.dates = dates;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }
}

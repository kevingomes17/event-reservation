package com.twa.evtreg.models.dto;

import com.twa.evtreg.models.AvailabilitySearch;

import java.util.Date;

public class AvailabilitySearchReq extends AvailabilitySearch {
    public AvailabilitySearchReq() {
        super();
    }

    public AvailabilitySearchReq(Date start, Date end) {
        super(start, end);
    }
}

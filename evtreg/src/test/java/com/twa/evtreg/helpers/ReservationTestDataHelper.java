package com.twa.evtreg.helpers;

import com.twa.evtreg.models.entities.Reservation;

import java.util.Calendar;
import java.util.Date;

public class ReservationTestDataHelper {
    public Reservation generate1DayReservation(int numDaysFromToday) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, numDaysFromToday);
        Date tomorrow = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date tomorrowPlus1 = cal.getTime();
        return new Reservation(null, "john@does.com", "John Doe", tomorrow, tomorrowPlus1);
    }

    public Reservation generate4DayReservation(int numDaysFromToday) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, numDaysFromToday);
        Date tomorrow = cal.getTime();
        cal.add(Calendar.DATE, 10);
        Date tomorrowPlus1 = cal.getTime();
        return new Reservation(null, "john@does.com", "John Doe", tomorrow, tomorrowPlus1);
    }
}

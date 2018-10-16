package com.twa.evtreg.services;

import com.twa.evtreg.exception.*;
import com.twa.evtreg.models.Availability;
import com.twa.evtreg.models.Reservation;
import com.twa.evtreg.repositories.AvailabilityRepository;
import com.twa.evtreg.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;


@Service
public class ReservationService {
    @Autowired
    AvailabilityRepository availabilityRepo;

    @Autowired
    ReservationRepository reservationRepo;

    private Boolean isBookingInAdvance(Reservation reservation) {
        Boolean flag = false;
        Date today = new Date();
        if (today.before(reservation.getArrivalDate())) {
            long diffDays = this.calculateDiffInDays(today, reservation.getArrivalDate());
            if (diffDays >= 1 && diffDays <= 30) {
                flag = true;
            }
        }
        return flag;
    }

    private Boolean isDurationValid(Reservation reservation) {
        Boolean flag = false;
        long durationDays = this.calculateDiffInDays(reservation.getDepartureDate(), reservation.getDepartureDate());
        if (durationDays <= 3) {
            flag = true;
        }
        return flag;
    }

    /**
     * Determines whether the Reservation dates are still available.
     * To be used prior to booking.
     * isBookingInAdvance(), isDurationValid() should have been invoked prior to invoking this method.
     * @param reservation
     * @return
     */
    private Boolean areDatesAvailable(Reservation reservation) {
        List<Availability> availabilityList = this.availabilityRepo.findAvailablityByDateRange(reservation.getArrivalDate(), reservation.getDepartureDate());
        Long numDays = this.calculateDiffInDays(reservation.getArrivalDate(), reservation.getDepartureDate());
        if (numDays == availabilityList.size()) {
            return true;
        } else {
            return false;
        }
    }

    private Long calculateDiffInDays(Date start, Date end) {
        long diffInDays = (end.getTime() - start.getTime())/(1000 * 60 * 60 *24);
        return diffInDays;
    }

    /**
     * Validates a reservation prior to booking a new one or updating an existing one.
     * @param reservation
     * @return
     * @throws RuntimeException
     */
    private void validate(Reservation reservation) throws RuntimeException {
        Boolean advanceBookingFlag = this.isBookingInAdvance(reservation);
        Boolean durationFlag = this.isDurationValid(reservation);
        Boolean datesAvailableFlag = this.areDatesAvailable(reservation);

        if (!advanceBookingFlag) throw new AdvanceReservationException("Reservation can only be booked 1 to 30 days in advance");
        if (!durationFlag) throw new ReservationDurationException("Reservation is allowed only for 3 days!");
        if (!datesAvailableFlag) throw new DatesNotAvailableException("The selected dates are no longer available!");
    }

    private Reservation exists(Long reservationId) throws RuntimeException {
        Optional<Reservation> reservation = reservationRepo.findById(reservationId);
        if (!reservation.isPresent()) throw new ReservationNotFoundException("Reservation not found!");
        return reservation.get();
    }

    /**
     * @param res
     */
    private void updateAvailability(Reservation res, Boolean isAvailable) {
        // TODO: Find all reservations greater than equal to today, since we're not interested in past reservations
        Availability availability;
        List<Availability> availabilityList = this.availabilityRepo.findAvailablityByDateRange(res.getArrivalDate(), res.getDepartureDate());
        Iterator availablityListLooper = availabilityList.iterator();
        while (availablityListLooper.hasNext()) {
            availability = (Availability) availablityListLooper.next();
            availability.setAvailable(isAvailable);
            this.availabilityRepo.save(availability);
        }
    }

    /**
     * TODO: Consider making this transactional
     * @param reservation
     * @return
     * @throws RuntimeException
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Reservation book(Reservation reservation) throws RuntimeException {
        this.validate(reservation);
        Reservation newReservation = reservationRepo.save(reservation);
        this.updateAvailability(newReservation, false);
        return newReservation;
    }

    public List<Reservation> fetchAll() {
        return reservationRepo.findAll();
    }

    /**
     * TODO: Consider making this transactional
     * @param reservation
     * @return
     * @throws RuntimeException
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Reservation update(Reservation reservation) throws RuntimeException {
        Long reservationId = reservation.getId();
        if (reservationId == null) throw new ReservationInvalidException("Reservation ID must be specified");
        this.exists(reservationId);
        this.validate(reservation);
        this.updateAvailability(reservation, true);
        reservationRepo.save(reservation);
        this.updateAvailability(reservation, false);
        return reservation;
    }

    /**
     * TODO: Consider making this transactional
     * @param reservationId
     * @return
     * @throws RuntimeException
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Boolean cancel(Long reservationId) throws RuntimeException {
        Reservation reservation = this.exists(reservationId);
        reservationRepo.deleteById(reservationId);
        this.updateAvailability(reservation, true);
        return true;
    }

    /**
     * Initializes the availability for 30 days from today.
     * TODO: Consider making this transactional
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void initAvailability() {
        this.availabilityRepo.deleteAll();

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        for (int i = 0;i < 30;i++) {
            this.availabilityRepo.save(new Availability(null, cal.getTime(), true));
            cal.add(Calendar.DATE, 1);
        }

        // TODO: Check if yesterday needs to be take into account
        List<Reservation> reservations = reservationRepo.findReservationByArrivalDate(today);
        Iterator reservationLooper = reservations.iterator();
        while (reservationLooper.hasNext()) {
            this.updateAvailability((Reservation) reservationLooper.next(), false);
        }
    }

    /**
     * Calculates the available dates within the Start & End Date range.
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Date> getAvailableDates(Date startDate, Date endDate) {
        List<Date> calendarDates;
        List<Availability> availabilityList = this.availabilityRepo.findAvailablityByDateRange(startDate, endDate);
        calendarDates = new ArrayList<>();
        Iterator availabilityLooper = availabilityList.iterator();
        while (availabilityLooper.hasNext()) {
            Availability avail = (Availability) availabilityLooper.next();
            if (avail.getAvailable()) {
                calendarDates.add(avail.getVenueDate());
            }
        }
        return calendarDates;
    }
}

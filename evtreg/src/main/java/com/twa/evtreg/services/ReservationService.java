package com.twa.evtreg.services;

import com.twa.evtreg.exception.*;
import com.twa.evtreg.models.dto.AvailabilitySearchRes;
import com.twa.evtreg.models.entities.Availability;
import com.twa.evtreg.models.entities.Reservation;
import com.twa.evtreg.repositories.AvailabilityRepository;
import com.twa.evtreg.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.Semaphore;


@Service
public class ReservationService {
    @Autowired
    AvailabilityRepository availabilityRepo;

    @Autowired
    ReservationRepository reservationRepo;

    private Semaphore lock = new Semaphore(1);
    private int MAX_ADVANCE_BOOKING_DAYS = 30;
    private int MIN_ADVANCE_BOOKING_DAYS = 0;
    private int MAX_BOOKING_DURATION_DAYS = 3;
    private int MIN_BOOKING_DURATION_DAYS = 1;

    private Boolean isBookingInAdvance(Reservation reservation) {
        Boolean flag = false;
        Date today = new Date();
        if (today.before(reservation.getArrivalDate())) {
            Long diffDays = this.calculateDiffInDays(today, reservation.getArrivalDate());
            if (diffDays >= MIN_ADVANCE_BOOKING_DAYS && diffDays <= MAX_ADVANCE_BOOKING_DAYS) {
                flag = true;
            }
        }
        return flag;
    }

    private Boolean isDurationValid(Reservation reservation) {
        Boolean flag = false;
        long durationDays = this.calculateDiffInDays(reservation.getArrivalDate(), reservation.getDepartureDate());
        if (durationDays >= MIN_BOOKING_DURATION_DAYS && durationDays <= MAX_BOOKING_DURATION_DAYS) {
            flag = true;
        }
        return flag;
    }

    /**
     * Determines whether the Reservation dates are still available.
     * @param reservation
     * @return
     */
    private Boolean areDatesAvailable(Reservation reservation) {
        List<Availability> availabilityList = this.availabilityRepo.findAvailablityByDateRange(
                true,
                reservation.getArrivalDate(), reservation.getDepartureDate());
        Long numDays = this.calculateDiffInDays(reservation.getArrivalDate(), reservation.getDepartureDate());
        if (availabilityList.size() >= numDays) {
            return true;
        } else {
            return false;
        }
    }

    private Long calculateDiffInDays(Date start, Date end) {
        long diffInDays = (end.getTime() - start.getTime())/(1000 * 60 * 60 *24);
        return diffInDays;
    }

    private void validateAvailability(Reservation reservation) throws RuntimeException {
        Boolean datesAvailableFlag = this.areDatesAvailable(reservation);
        if (!datesAvailableFlag) throw new DatesNotAvailableException("The selected dates are no longer available. Please check availability and try again.");
    }

    private Reservation exists(Long reservationId) throws RuntimeException {
        Optional<Reservation> reservation = reservationRepo.findById(reservationId);
        if (!reservation.isPresent()) throw new ReservationNotFoundException("Reservation not found");
        return reservation.get();
    }

    /**
     * Updates the availability of a Date.
     * @param res
     * @param isAvailable
     */
    private void updateAvailability(Reservation res, Boolean isAvailable) {
        Availability availability;
        List<Availability> availabilityList = this.availabilityRepo.findAvailablityByDateRange(
                !isAvailable,
                res.getArrivalDate(), res.getDepartureDate());
        Iterator availabilityListLooper = availabilityList.iterator();
        while (availabilityListLooper.hasNext()) {
            availability = (Availability) availabilityListLooper.next();
            availability.setAvailable(isAvailable);
            this.availabilityRepo.save(availability);
        }
    }

    private Date resetTime(Date date) {
        return this.setTime(date, 0, 0, 0);
    }

    private Date setTime(Date date, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Validates a reservation prior to booking a new reservation.
     * @param reservation
     * @return
     * @throws RuntimeException
     */
    public boolean validate(Reservation reservation) throws RuntimeException {
        boolean advanceBookingFlag = this.isBookingInAdvance(reservation);
        if (!advanceBookingFlag) throw new AdvanceReservationException("Reservation can only be booked 1 to 30 days in advance");

        boolean durationFlag = this.isDurationValid(reservation);
        if (!durationFlag) throw new ReservationDurationException("Reservation must be booked for at least 1 day & up to 3 days");

        return true;
    }

    /**
     * Validates an existing reservation prior to booking.
     * @param reservation
     * @throws RuntimeException
     */
    public boolean validateExisting(Reservation reservation) throws RuntimeException {
        Long reservationId = reservation.getId();
        if (reservationId == null) throw new ReservationInvalidException("Reservation ID must be specified");

        Reservation r2 = this.exists(reservation.getId());
        if (r2.equals(reservation)) throw new NoChangeException("No change made to the reservation");

        return this.validate(reservation);
    }

    /**
     * Books a new reservation and updates availability accordingly.
     * Does not validate "Duration" or "Booking In Advance". This is handled in the REST API Controller.
     * .. This is done in order to handle race condition and yet maximize system availability.
     * @param reservation
     * @return
     * @throws RuntimeException
     * @see this.validate()
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Reservation book(Reservation reservation) throws RuntimeException {
        this.validateAvailability(reservation);
        reservationRepo.save(reservation);
        this.updateAvailability(reservation, false);
        return reservation;
    }

    public List<Reservation> fetchAll() {
        return reservationRepo.findAll();
    }

    /**
     * Updates an existing reservation and the corresponding availability.
     * Does not validate Duration or Booking In Advance. This is handled in the REST API Controller.
     * .. This is done in order to handle race condition and yet maximize system availability.
     * @param reservation
     * @return
     * @throws RuntimeException
     * @see this.validateExisting()
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Reservation update(Reservation reservation) throws RuntimeException {
        Reservation r2 = this.exists(reservation.getId());
        if (!r2.areReservationDatesEqual(reservation)) {
            this.updateAvailability(reservation, true);
            this.validateAvailability(reservation);
            reservationRepo.save(reservation);
            this.updateAvailability(reservation, false);
        } else {
            reservationRepo.save(reservation);
        }
        return reservation;
    }

    /**
     * Deletes a reservation and updates corresponding availability.
     * @param reservationId
     * @return
     * @throws RuntimeException
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Boolean cancel(Long reservationId) throws RuntimeException {
        Reservation reservation = this.exists(reservationId);
        this.updateAvailability(reservation, true);
        reservationRepo.deleteById(reservationId);
        return true;
    }

    /**
     * Initializes the availability for 30 days from today.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void initAvailability() {
        this.availabilityRepo.deleteAll();

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        for (int i = 0;i < 30;i++) {
            cal.add(Calendar.DATE, 1);
            this.availabilityRepo.save(
                new Availability(null, this.resetTime(cal.getTime()), true)
            );
        }

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
        List<Availability> availabilityList = this.availabilityRepo.findAvailablityByDateRange(true, startDate, endDate);
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

    /**
     * Fetches availability for 30 days, starting from tomorrow.
     * @return
     */
    public AvailabilitySearchRes getAvailableDates() {
        Date start = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, MAX_ADVANCE_BOOKING_DAYS);
        Date end = cal.getTime();
        List<Date> dates = this.getAvailableDates(this.resetTime(start), this.resetTime(end));
        return new AvailabilitySearchRes(start, end, dates);
    }

    public void lock() throws RuntimeException {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Problem demonstrating streamlining Reservation booking.");
        }
    }

    public void unlock() {
        lock.release();
    }
}

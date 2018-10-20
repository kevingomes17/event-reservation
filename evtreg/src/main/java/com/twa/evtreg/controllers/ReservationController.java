package com.twa.evtreg.controllers;

import com.twa.evtreg.models.entities.Reservation;
import com.twa.evtreg.models.dto.*;
import com.twa.evtreg.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService service;

    /**
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/available-dates", method = RequestMethod.GET, params = {"start", "end"})
    public AvailabilitySearchRes getAvailableDates(AvailabilitySearchReq req) {
        List<Date> dates = service.getAvailableDates(req.getStart(), req.getEnd());
        return new AvailabilitySearchRes(req.getStart(), req.getEnd(), dates);
    }

    @RequestMapping(value = "/available-dates", method = RequestMethod.GET)
    public AvailabilitySearchRes getAvailableDatesFor() {
        return service.getAvailableDates();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<ReservationRes> list() {
        List<ReservationRes> res = new ArrayList<>();

        List<Reservation> reservations = service.fetchAll();
        Iterator<Reservation> reservationLooper = reservations.iterator();
        while(reservationLooper.hasNext()) {
            res.add(new ReservationRes(reservationLooper.next()));
        }
        return res;
    }

    /**
     * Book a reservation. Handles race condition.
     * @param req
     * @return
     */
    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public ReservationRes book(@Valid @RequestBody ReservationReq req) {
        service.validate(req);
        service.lock();
        Reservation res;
        try {
            res = service.book(req);
        } finally {
            service.unlock();
        }
        return new ReservationRes(res);
    }

    /**
     * Book a reservation. Doesn't handle race condition.
     * @param req
     * @return
     */
    @RequestMapping(value = "/book-faulty", method = RequestMethod.POST)
    public ReservationRes bookFaulty(@Valid @RequestBody ReservationReq req) {
        service.validate(req);
        Reservation res;
        res = service.book(req);
        return new ReservationRes(res);
    }

    /**
     *
     * @param id
     * @param req
     * @return
     */
    @RequestMapping(value = "/change/{id}", method = RequestMethod.PUT)
    public ReservationRes change(@PathVariable("id") Long id, @Valid @RequestBody ReservationReq req) {
        req.setId(id);
        service.validateExisting(req);
        service.lock();
        Reservation res;
        try {
            res = service.update(req);
        } finally {
            service.unlock();
        }
        return new ReservationRes(res);
    }

    /**
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.DELETE)
    public BasicRes cancel(@PathVariable("id") Long id) {
        Boolean flag = service.cancel(id);
        return new BasicRes(flag, flag ? "Successfully cancelled reservation!" : "Unable to cancel reservation", null);
    }

    /**
     * Acquires the Semaphore lock.
     * Used to demo other APIs' availability while Book/Update Reservation might have locked access to handle race condition.
     * @return
     */
    @RequestMapping(value = "/lock", method = RequestMethod.GET)
    public BasicRes lock() {
        service.lock();
        return new BasicRes(true, "Locked Semaphore", null);
    }

    /**
     * Releases the Semaphore lock.
     * Used to demo other APIs' availability while Book/Update Reservation might have locked access to handle race condition.
     * @return
     */
    @RequestMapping(value = "/unlock", method = RequestMethod.GET)
    public BasicRes unlock() {
        service.unlock();
        return new BasicRes(true, "Unlocked Semaphore", null);
    }
}

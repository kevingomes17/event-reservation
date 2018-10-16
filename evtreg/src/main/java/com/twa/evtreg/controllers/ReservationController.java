package com.twa.evtreg.controllers;

import com.twa.evtreg.models.Reservation;
import com.twa.evtreg.models.dto.*;
import com.twa.evtreg.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
     * TODO: Validate whether the dates are still available
     * TODO: Check if this method can be multi-threaded
     * @param req
     * @return
     */
    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public ReservationRes book(@Valid @RequestBody ReservationReq req) {
        Reservation res = service.book(req);
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
        Reservation res = service.update(req);
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
}

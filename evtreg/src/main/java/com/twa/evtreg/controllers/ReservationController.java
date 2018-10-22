package com.twa.evtreg.controllers;

import com.twa.evtreg.models.entities.Reservation;
import com.twa.evtreg.models.dto.*;
import com.twa.evtreg.services.ReservationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    @RequestMapping(value = "/available-dates",
            method = RequestMethod.GET,
            params = {"start", "end"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Check availability",
            notes = "Check availability either by specifying a date range. Or omitting the Date Range will fetch availability for the next 30 days."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = AvailabilitySearchRes.class)
    })
    public AvailabilitySearchRes getAvailableDates(AvailabilitySearchReq req) {
        List<Date> dates = service.getAvailableDates(req.getStart(), req.getEnd());
        return new AvailabilitySearchRes(req.getStart(), req.getEnd(), dates);
    }

    @RequestMapping(value = "/available-dates",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Check availability",
            notes = "Check availability either by specifying a date range. Or omitting the Date Range will fetch availability for the next 30 days."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = AvailabilitySearchRes.class)
    })
    public AvailabilitySearchRes getAvailableDatesFor() {
        return service.getAvailableDates();
    }

    @RequestMapping(value = "/list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "View Reservations",
            notes = "Displays all the reservations made."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = ReservationRes[].class)
    })
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
    @RequestMapping(value = "/book",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Book Reservation",
            notes = "Books a reservation after all the validation criteria are met. Handles Race Condition."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created", response = ReservationRes.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Validation failed. Dates not available."),
            @ApiResponse(code = 406, message = "Validation failed.")
    })
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
    @RequestMapping(value = "/book-faulty",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Book Reservation",
            notes = "Books a reservation after all the validation criteria are met. Does not handle Race Condition."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created", response = ReservationRes.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Validation failed. Dates not available."),
            @ApiResponse(code = 406, message = "Validation failed.")
    })
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
    @RequestMapping(value = "/change/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Change Reservation",
            notes = "Allows you to update a reservation after all the validation criteria are met. Handle Race Condition."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created", response = ReservationRes.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Validation failed. Reservation not found or Dates not available."),
            @ApiResponse(code = 406, message = "Validation failed. Or No change in reservation.")
    })
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
    @RequestMapping(value = "/cancel/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Cancel Reservation",
            notes = "Cancels a reservation."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Cancelled Reservation", response = BasicRes.class),
            @ApiResponse(code = 404, message = "Not found")
    })
    public BasicRes cancel(@PathVariable("id") Long id) {
        Boolean flag = service.cancel(id);
        return new BasicRes(flag, flag ? "Successfully cancelled reservation!" : "Unable to cancel reservation", null);
    }

    /**
     * Acquires the Semaphore lock.
     * Used to demo other APIs' availability while Book/Update Reservation might have locked access to handle race condition.
     * @return
     */
    @RequestMapping(value = "/lock",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Lock Reservation Booking or Changing",
            notes = "Locks the booking or changing of any reservation. Used to demonstrate that the Check Availability continues to work even though the Reservation Booking/Updating is locked."
    )
    public BasicRes lock() {
        service.lock();
        return new BasicRes(true, "Locked Semaphore", null);
    }

    /**
     * Releases the Semaphore lock.
     * Used to demo other APIs' availability while Book/Update Reservation might have locked access to handle race condition.
     * @return
     */
    @RequestMapping(value = "/unlock",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Unlock Reservation Booking or Changing",
            notes = "Unlocks the booking or changing of any reservation."
    )
    public BasicRes unlock() {
        service.unlock();
        return new BasicRes(true, "Unlocked Semaphore", null);
    }
}

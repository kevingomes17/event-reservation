package com.twa.evtreg.controllers;

import com.twa.evtreg.exception.*;
import com.twa.evtreg.helpers.ReservationTestDataHelper;
import com.twa.evtreg.models.dto.AvailabilitySearchRes;
import com.twa.evtreg.models.entities.Reservation;
import com.twa.evtreg.services.ReservationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReservationService service;

    ReservationTestDataHelper testDataHelper = new ReservationTestDataHelper();

    @Test
    public void checkAvailability_noParameters() throws Exception {
        Reservation reservation = this.testDataHelper.generate1DayReservation(1);
        AvailabilitySearchRes res = new AvailabilitySearchRes(
                reservation.getArrivalDate(), reservation.getDepartureDate(), Arrays.asList(reservation.getArrivalDate()));
        given(service.getAvailableDates()).willReturn(res);

        mvc.perform(get("/reservation/available-dates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates", hasSize(1)));
    }

    @Test
    public void checkAvailability_withParameters() throws Exception {
        Date today = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        List<Date> dates = Arrays.asList(reservation.getArrivalDate());
        given(service.getAvailableDates(Mockito.any(Date.class), Mockito.any(Date.class))).willReturn(dates);

        mvc.perform(get(String.format("/reservation/available-dates?start=%s&end=%s", dateFormatter.format(today), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates", hasSize(1)));
    }

    @Test
    public void bookReservation_invalidRequest() throws Exception {
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);

        mvc.perform(post("/reservation/book")
                .content("{\"name\": \"John Doe\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void bookReservation_validRequest() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);
        given(service.book(Mockito.any(Reservation.class))).willReturn(reservation);

        mvc.perform(post("/reservation/book")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"%s\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getArrivalDate()), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void bookReservation_invalidArrivalDateRequest() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);
        given(service.validate(Mockito.any(Reservation.class))).willThrow(new AdvanceReservationException("Invalid"));

        mvc.perform(post("/reservation/book")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"2017-10-19\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void bookReservation_invalidDurationRequest() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);
        given(service.validate(Mockito.any(Reservation.class))).willThrow(new ReservationDurationException("Invalid"));

        mvc.perform(post("/reservation/book")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"2017-10-19\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void bookReservation_validRequest_datesNotAvailable() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);
        given(service.book(Mockito.any(Reservation.class))).willThrow(new DatesNotAvailableException("Invalid"));

        mvc.perform(post("/reservation/book")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"2017-10-19\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getArrivalDate()), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeReservation_invalidRequest() throws Exception {
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);

        mvc.perform(put("/reservation/change/10")
                .content("{\"name\": \"John Doe\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeReservation_validRequest() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(10L);
        given(service.update(Mockito.any(Reservation.class))).willReturn(reservation);

        mvc.perform(put("/reservation/change/10")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"%s\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getArrivalDate()), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void changeReservation_invalidArrivalDateRequest() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(10L);
        given(service.validateExisting(Mockito.any(Reservation.class))).willThrow(new AdvanceReservationException("Invalid"));

        mvc.perform(put("/reservation/change/10")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"2017-10-19\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeReservation_invalidDurationRequest() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(10L);
        given(service.validateExisting(Mockito.any(Reservation.class))).willThrow(new ReservationDurationException("Invalid"));

        mvc.perform(put("/reservation/change/10")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"2017-10-19\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeReservation_validRequest_datesNotAvailable() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(10L);
        given(service.update(Mockito.any(Reservation.class))).willThrow(new DatesNotAvailableException("Invalid"));

        mvc.perform(put("/reservation/change/10")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"%s\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getArrivalDate()), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeReservation_validRequest_noChangesMade() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(10L);
        given(service.update(Mockito.any(Reservation.class))).willThrow(new NoChangeException("Invalid"));

        mvc.perform(put("/reservation/change/10")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"%s\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getArrivalDate()), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void changeReservation_validRequest_invalidId() throws Exception {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Reservation reservation = this.testDataHelper.generate1DayReservation(2);
        reservation.setId(91L);
        given(service.update(Mockito.any(Reservation.class))).willThrow(new NoChangeException("Invalid"));

        mvc.perform(put("/reservation/change")
                .content(String.format("{\"name\": \"John Doe\", \"email\": \"john@doe.com\", \"arrivalDate\": \"%s\", \"departureDate\": \"%s\"}",
                        dateFormatter.format(reservation.getArrivalDate()), dateFormatter.format(reservation.getDepartureDate())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void cancelReservation_invalidRequest_invalidId() throws Exception {
        given(service.cancel(Mockito.any(Long.class))).willThrow(new ReservationNotFoundException("Invalid"));

        mvc.perform(delete("/reservation/cancel/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void cancelReservation_validRequest() throws Exception {
        Reservation reservation = this.testDataHelper.generate1DayReservation(0);
        given(service.cancel(Mockito.any(Long.class))).willReturn(true);

        mvc.perform(delete("/reservation/cancel/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void lockReservation() throws Exception {
        mvc.perform(get("/reservation/lock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void unlockReservation() throws Exception {
        mvc.perform(get("/reservation/unlock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

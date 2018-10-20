package com.twa.evtreg.services;

import com.twa.evtreg.helpers.ReservationTestDataHelper;
import com.twa.evtreg.models.dto.AvailabilitySearchRes;
import com.twa.evtreg.models.entities.Reservation;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@DataJpaTest
public class ReservationServiceTest {

    @TestConfiguration
    static class ReservationServiceTestContextConfiguration {

        @Bean
        public ReservationService service() {
            return new ReservationService();
        }
    }

    @Autowired
    ReservationService service;

    ReservationTestDataHelper testDataHelper = new ReservationTestDataHelper();

    @Before
    public void initHelper() {
        service.initAvailability();
    }

    @Test
    public void a_checkAvailability_whenNoFutureReservationsExist() {
        AvailabilitySearchRes availabilityResults = service.getAvailableDates();
        Assertions.assertThat(availabilityResults.getDates().size()).isEqualTo(30);
    }

    @Test
    public void b_checkAvailability_whenFutureReservationExists() {
        Reservation reservation = service.book(this.testDataHelper.generate1DayReservation(1));
        AvailabilitySearchRes availabilityResults = service.getAvailableDates();
        service.cancel(reservation.getId());
        Assertions.assertThat(availabilityResults.getDates().size()).isEqualTo(29);
    }

    @Test
    public void bookReservation_whenDatesAreAvailable() {
        Reservation reservation = service.book(this.testDataHelper.generate1DayReservation(2));
        Assertions.assertThat(reservation).isNotNull();
    }

    @Test
    public void validateReservation_whenDurationIsInvalid() {
        boolean isDurationValid = true;
        Reservation reservation = this.testDataHelper.generate4DayReservation(5);
        try {
            service.validate(reservation);
        } catch(RuntimeException e) {
            // do nothing.
            isDurationValid = false;
        }
        Assertions.assertThat(isDurationValid).isEqualTo(false);
    }

    @Test
    public void validateReservation_whenDepartureIsGreaterThanArrival() {
        boolean isDurationValid = true;
        Reservation reservation = this.testDataHelper.generate4DayReservation(5);
        Date arrivalDate = reservation.getArrivalDate();
        Date departureDate = reservation.getDepartureDate();
        try {
            reservation.setArrivalDate(departureDate);
            reservation.setDepartureDate(arrivalDate);
            service.validate(reservation);
        } catch(RuntimeException e) {
            // do nothing.
            isDurationValid = false;
        }
        Assertions.assertThat(isDurationValid).isEqualTo(false);
    }

    @Test
    public void validateReservation_whenBookingIsBeyond30DaysFromToday() {
        boolean isInAdvance = true;
        Reservation reservation = this.testDataHelper.generate1DayReservation(31);
        try {
            service.validate(reservation);
        } catch(RuntimeException e) {
            // do nothing.
            isInAdvance = false;
        }
        Assertions.assertThat(isInAdvance).isEqualTo(false);
    }

    @Test
    public void validateReservation_whenBookingForToday() {
        boolean isInAdvance = true;
        Reservation reservation = this.testDataHelper.generate1DayReservation(0);
        try {
            service.validate(reservation);
        } catch(RuntimeException e) {
            // do nothing.
            isInAdvance = false;
        }
        Assertions.assertThat(isInAdvance).isEqualTo(false);
    }

    @Test
    public void updateReservation_WithoutAnyChanges() {
        boolean flag = true;
        try {
            Reservation reservation = service.book(this.testDataHelper.generate1DayReservation(3));
            service.validateExisting(reservation);
            service.cancel(reservation.getId());
        } catch(RuntimeException e) {
            // do nothing.
            flag = false;
        }
        Assertions.assertThat(flag).isEqualTo(false);
    }

    @Test
    public void updateReservation_WithOnlyNameChange() {
        boolean flag = true;
        try {
            Reservation reservation = service.book(this.testDataHelper.generate1DayReservation(7));
            reservation.setName("Sarah Jones");
            service.validateExisting(reservation);
            service.cancel(reservation.getId());
        } catch(RuntimeException e) {
            // do nothing.
            System.out.println(e.getMessage());
            flag = false;
        }
        Assertions.assertThat(flag).isEqualTo(true);
    }

    @Test
    public void updateReservation_WithValidDatesChanged() {
        boolean flag = true;
        try {
            Reservation reservation = service.book(this.testDataHelper.generate1DayReservation(7));

            Reservation newReservation = this.testDataHelper.generate1DayReservation(6);
            newReservation.setId(reservation.getId());
            service.validateExisting(newReservation);
            service.cancel(newReservation.getId());
        } catch(RuntimeException e) {
            // do nothing.
            System.out.println(e.getMessage());
            flag = false;
        }
        Assertions.assertThat(flag).isEqualTo(true);
    }

    @Test
    public void cancelReservation_whenReservationExists() {
        boolean flag = false;
        try {
            Reservation reservation = this.testDataHelper.generate1DayReservation(9);
            service.book(reservation);
            flag = service.cancel(reservation.getId());

            List<Date> availabilityDates = service.getAvailableDates(reservation.getArrivalDate(), reservation.getDepartureDate());
            if (availabilityDates.size() == 0) {
                flag = false;
            }
        } catch(RuntimeException e) {
            flag = false;
        }
        Assertions.assertThat(flag).isEqualTo(true);
    }

    @Test
    public void cancelReservation_whenReservationDoesNotExist() {
        boolean flag = false;
        try {
            flag = service.cancel(99L);
        } catch(RuntimeException e) {
            // do nothing.
        }
        Assertions.assertThat(flag).isEqualTo(false);
    }
}

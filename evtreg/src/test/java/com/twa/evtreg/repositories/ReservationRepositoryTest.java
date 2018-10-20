package com.twa.evtreg.repositories;

import com.twa.evtreg.helpers.ReservationTestDataHelper;
import com.twa.evtreg.models.entities.Reservation;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReservationRepositoryTest {
    @Autowired
    ReservationRepository reservationRepo;

    ReservationTestDataHelper testDataHelper;

    @Before
    public void initHelper() {
        this.testDataHelper = new ReservationTestDataHelper();
    }

    @Test
    public void findReservations_greaterThanSpecifiedDate() {
        List<Reservation> reservations = reservationRepo.findReservationByArrivalDate(new Date());
        Assertions.assertThat(reservations.size()).isEqualTo(0);

        reservationRepo.save(this.testDataHelper.generate1DayReservation(1));
        List<Reservation> reservations2 = reservationRepo.findReservationByArrivalDate(new Date());
        Assertions.assertThat(reservations2.size()).isEqualTo(1);
    }
}

package com.twa.evtreg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twa.evtreg.EvtregApplication;
import com.twa.evtreg.helpers.BookReservationCallable;
import com.twa.evtreg.helpers.ReservationTestDataHelper;
import com.twa.evtreg.models.dto.AvailabilitySearchRes;
import com.twa.evtreg.models.entities.Reservation;
import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {EvtregApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ReservationControllerIntegrationTest {
    @LocalServerPort
    private int port;

    ReservationTestDataHelper testDataHelper = new ReservationTestDataHelper();
    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();

    private String createURIWithPort(String endpoint) {
        return String.format("http://localhost:%d%s", port, endpoint);
    }

    @Test
    public void a_checkAvailability() throws IOException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURIWithPort("/reservation/available-dates"), HttpMethod.GET, entity, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ObjectMapper mapper = new ObjectMapper();
        AvailabilitySearchRes availabilitySearchRes = mapper.readValue(response.getBody(), AvailabilitySearchRes.class);
        Assertions.assertThat(availabilitySearchRes.getDates().size()).isEqualTo(30);
    }

    @Test
    public void a_checkAvailability_whileReservationBookingIsLocked() {
        HttpEntity<String> lockEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> lockResponse = restTemplate.exchange(
                createURIWithPort("/reservation/available-dates"), HttpMethod.GET, lockEntity, String.class);
        Assertions.assertThat(lockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURIWithPort("/reservation/available-dates"), HttpMethod.GET, entity, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpEntity<String> unlockEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> unlockResponse = restTemplate.exchange(
                createURIWithPort("/reservation/available-dates"), HttpMethod.GET, unlockEntity, String.class);
        Assertions.assertThat(unlockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void bookReservation() {
        Reservation reservation = testDataHelper.generate1DayReservation(3);
        HttpEntity<Reservation> entity = new HttpEntity<>(reservation, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURIWithPort("/reservation/book"), HttpMethod.POST, entity, String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void bookReservation_handlesRaceCondition() throws InterruptedException, ExecutionException {
        Reservation reservation1 = testDataHelper.generate1DayReservation(1);
        Reservation reservation2 = testDataHelper.generate1DayReservation(1);

        // Reservation Request 1 will be sent after Reservation 2
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<ResponseEntity<String>> response1Future = executorService.submit(new BookReservationCallable("Reservation 1",
                createURIWithPort("/reservation/book"), reservation1, headers, restTemplate, 1000));
        Future<ResponseEntity<String>> response2Future = executorService.submit(new BookReservationCallable("Reservation 2",
                createURIWithPort("/reservation/book"), reservation2, headers, restTemplate, 999));

        if (response1Future.get().getStatusCode() == HttpStatus.OK) {
            Assertions.assertThat(response2Future.get().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        } else {
            Assertions.assertThat(response2Future.get().getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    public void bookReservation_doesNotHandleRaceCondition() throws InterruptedException, ExecutionException {
        Reservation reservation1 = testDataHelper.generate1DayReservation(5);
        Reservation reservation2 = testDataHelper.generate1DayReservation(5);

        // Attempting to send 2 reservation requests at the same time.
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<ResponseEntity<String>> response1Future = executorService.submit(new BookReservationCallable("Reservation 1",
                createURIWithPort("/reservation/book-faulty"), reservation1, headers, restTemplate, 1000));
        Future<ResponseEntity<String>> response2Future = executorService.submit(new BookReservationCallable("Reservation 2",
                createURIWithPort("/reservation/book-faulty"), reservation2, headers, restTemplate, 999));
        Assertions.assertThat(response1Future.get().getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response2Future.get().getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

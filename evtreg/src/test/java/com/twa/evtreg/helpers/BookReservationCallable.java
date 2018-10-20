package com.twa.evtreg.helpers;

import com.twa.evtreg.models.entities.Reservation;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Calendar;
import java.util.concurrent.Callable;

public class BookReservationCallable implements Callable<ResponseEntity<String>> {
    private String name;
    private String url;
    private Reservation reservation;
    HttpHeaders httpHeaders;
    TestRestTemplate restTemplate;
    long sleepMs;

    public BookReservationCallable(String name,
                                 String url, Reservation reservation, HttpHeaders httpHeaders,
                                 TestRestTemplate restTemplate, long sleepMs) {
        this.name = name;
        this.url = url;
        this.reservation = reservation;
        this.httpHeaders = httpHeaders;
        this.restTemplate = restTemplate;
        this.sleepMs = sleepMs;
    }

    public ResponseEntity<String> call() {
        try {
            Thread.sleep(sleepMs);
        } catch(InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        System.out.println("Book Reservation Thread: " + this.name + " : " + Calendar.getInstance().get(Calendar.MILLISECOND));
        HttpEntity<Reservation> entity = new HttpEntity<>(reservation, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println(this.name + " Response: " + response);
        return response;
    }
}

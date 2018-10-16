package com.twa.evtreg;

import com.twa.evtreg.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {
    @Autowired
    private ReservationService reservationService;

    public void run(ApplicationArguments args) {
        this.reservationService.initAvailability();
    }
}

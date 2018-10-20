package com.twa.evtreg;

import com.twa.evtreg.controllers.ReservationControllerIntegrationTest;
import com.twa.evtreg.controllers.ReservationControllerTest;
import com.twa.evtreg.repositories.AvailabilityRepositoryTest;
import com.twa.evtreg.repositories.ReservationRepositoryTest;
import com.twa.evtreg.services.ReservationServiceTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		AvailabilityRepositoryTest.class,
		ReservationRepositoryTest.class,
		ReservationServiceTest.class,
		ReservationControllerTest.class,
		ReservationControllerIntegrationTest.class
})
public class EvtregApplicationTests {}

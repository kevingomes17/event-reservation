package com.twa.evtreg.repositories;

import com.twa.evtreg.models.entities.Availability;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AvailabilityRepositoryTest {
    @Autowired
    AvailabilityRepository availabilityRepo;

    @Test
    public void findAvailableDates_whenDateRangeIsProvided() {
        Date today = new Date();
        availabilityRepo.save(new Availability(null, today, true));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 7);
        Date sevenDaysLater = cal.getTime();

        List<Availability> availabilityList = availabilityRepo.findAvailablityByDateRange(true, today, sevenDaysLater);
        Assertions.assertThat(availabilityList.size()).isEqualTo(1);
    }

    @Test
    public void findUnavailableDates_whenDateRangeIsProvided() {
        Date today = new Date();
        availabilityRepo.save(new Availability(null, today, false));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 7);
        Date sevenDaysLater = cal.getTime();

        List<Availability> availabilityList = availabilityRepo.findAvailablityByDateRange(false, today, sevenDaysLater);
        Assertions.assertThat(availabilityList.size()).isEqualTo(1);
    }
}

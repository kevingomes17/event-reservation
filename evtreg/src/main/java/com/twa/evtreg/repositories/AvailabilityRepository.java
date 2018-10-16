package com.twa.evtreg.repositories;

import com.twa.evtreg.models.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    @Query("SELECT a FROM Availability a WHERE a.isAvailable = true AND venueDate BETWEEN :fromDate AND :toDate")
    List<Availability> findAvailablityByDateRange(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}

package com.twa.evtreg.repositories;

import com.twa.evtreg.models.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT r FROM Reservation r WHERE r.arrivalDate > :fromDate")
    List<Reservation> findReservationByArrivalDate(@Param("fromDate") Date fromDate);
}

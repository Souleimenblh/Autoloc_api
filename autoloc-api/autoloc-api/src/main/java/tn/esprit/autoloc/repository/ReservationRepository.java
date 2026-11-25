package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.entity.Reservation;

// Spring Data repository: provides database operations for this entity.
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}

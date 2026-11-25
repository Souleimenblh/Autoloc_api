package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.entity.Vehicule;

// Spring Data repository: provides database operations for this entity.
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
}

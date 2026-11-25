package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.entity.Contrat;

// Spring Data repository: provides database operations for this entity.
public interface ContratRepository extends JpaRepository<Contrat, Long> {
}

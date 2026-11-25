package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.entity.Maintenance;

// Spring Data repository: provides database operations for this entity.
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
}

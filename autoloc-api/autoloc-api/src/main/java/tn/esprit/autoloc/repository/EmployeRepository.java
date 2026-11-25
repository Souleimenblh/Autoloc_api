package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.entity.Employe;

import java.util.List;

// Spring Data repository: provides database operations for this entity.
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    List<Employe> findByAgence_IdAgence(Long idAgence);
}


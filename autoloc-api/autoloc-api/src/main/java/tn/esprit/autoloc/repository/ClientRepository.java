package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.entity.Client;

// Spring Data repository: provides database operations for this entity.
public interface ClientRepository extends JpaRepository<Client, Long> {
}

package tn.stb.reclamations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.stb.reclamations.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
}

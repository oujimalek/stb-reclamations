package tn.stb.reclamations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.stb.reclamations.model.Role;
import tn.stb.reclamations.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRoleAndActifTrue(Role role);
}

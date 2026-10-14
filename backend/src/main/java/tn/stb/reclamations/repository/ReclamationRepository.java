package tn.stb.reclamations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.stb.reclamations.model.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    Optional<Reclamation> findByReference(String reference);
    List<Reclamation> findAllByOrderByDateCreationDesc();
    List<Reclamation> findByAgentOrderByDateCreationDesc(Utilisateur agent);
    long countByAgentAndStatutNotIn(Utilisateur agent, Collection<Statut> statuts);
    List<Reclamation> findByStatutNotInAndEscaladeeFalseAndDateEcheanceBefore(Collection<Statut> statuts, LocalDateTime date);

    @Query("select r.statut, count(r) from Reclamation r group by r.statut")
    List<Object[]> compterParStatut();

    @Query("select r.categorie, count(r) from Reclamation r group by r.categorie")
    List<Object[]> compterParCategorie();
}

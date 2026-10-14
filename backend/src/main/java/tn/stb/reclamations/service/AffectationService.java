package tn.stb.reclamations.service;

import org.springframework.stereotype.Service;
import tn.stb.reclamations.model.*;
import tn.stb.reclamations.repository.ReclamationRepository;
import tn.stb.reclamations.repository.UtilisateurRepository;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Affectation automatique : parmi les agents habilités sur la catégorie,
 * on choisit celui qui a le moins de réclamations ouvertes (équilibrage de charge).
 */
@Service
public class AffectationService {
    private static final EnumSet<Statut> FERMES = EnumSet.of(Statut.RESOLUE, Statut.CLOTUREE, Statut.REJETEE);

    private final UtilisateurRepository utilisateurs;
    private final ReclamationRepository reclamations;

    public AffectationService(UtilisateurRepository utilisateurs, ReclamationRepository reclamations) {
        this.utilisateurs = utilisateurs;
        this.reclamations = reclamations;
    }

    public Optional<Utilisateur> choisirAgent(Categorie categorie) {
        return utilisateurs.findByRoleAndActifTrue(Role.AGENT).stream()
                .filter(a -> a.getSpecialites().contains(categorie))
                .min(Comparator.comparingLong(a -> reclamations.countByAgentAndStatutNotIn(a, FERMES)));
    }
}

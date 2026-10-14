package tn.stb.reclamations.workflow;

import org.springframework.stereotype.Component;
import tn.stb.reclamations.model.Role;
import tn.stb.reclamations.model.Statut;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static tn.stb.reclamations.model.Statut.*;

/**
 * Machine à états du traitement d'une réclamation.
 * Centralise les transitions autorisées et les rôles habilités à les déclencher.
 */
@Component
public class WorkflowEngine {

    private final Map<Statut, Set<Statut>> transitions = new EnumMap<>(Statut.class);

    public WorkflowEngine() {
        transitions.put(NOUVELLE, EnumSet.of(QUALIFIEE, REJETEE));
        transitions.put(QUALIFIEE, EnumSet.of(AFFECTEE, REJETEE));
        transitions.put(AFFECTEE, EnumSet.of(EN_COURS));
        transitions.put(EN_COURS, EnumSet.of(EN_ATTENTE_CLIENT, ESCALADEE, RESOLUE));
        transitions.put(EN_ATTENTE_CLIENT, EnumSet.of(EN_COURS, CLOTUREE));
        transitions.put(ESCALADEE, EnumSet.of(EN_COURS, RESOLUE));
        transitions.put(RESOLUE, EnumSet.of(CLOTUREE, EN_COURS));
        transitions.put(CLOTUREE, EnumSet.noneOf(Statut.class));
        transitions.put(REJETEE, EnumSet.noneOf(Statut.class));
    }

    public Set<Statut> transitionsPossibles(Statut depuis) {
        return transitions.getOrDefault(depuis, Set.of());
    }

    public boolean estAutorisee(Statut depuis, Statut vers) {
        return transitionsPossibles(depuis).contains(vers);
    }

    /** Certaines transitions sont réservées au superviseur (rejet, reprise d'une réclamation escaladée). */
    public boolean roleHabilite(Statut depuis, Statut vers, Role role) {
        if (role == Role.ADMIN || role == Role.SUPERVISEUR) return true;
        if (vers == REJETEE) return false;
        if (depuis == ESCALADEE) return false;
        return true;
    }

    public void verifier(Statut depuis, Statut vers, Role role) {
        if (!estAutorisee(depuis, vers)) {
            throw new TransitionInvalideException("Transition interdite : " + depuis + " -> " + vers);
        }
        if (!roleHabilite(depuis, vers, role)) {
            throw new TransitionInvalideException("Le rôle " + role + " ne peut pas effectuer " + depuis + " -> " + vers);
        }
    }
}

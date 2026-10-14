package tn.stb.reclamations.workflow;

import org.junit.jupiter.api.Test;
import tn.stb.reclamations.model.Role;

import static org.junit.jupiter.api.Assertions.*;
import static tn.stb.reclamations.model.Statut.*;

class WorkflowEngineTest {
    private final WorkflowEngine wf = new WorkflowEngine();

    @Test
    void cheminNominalAutorise() {
        assertDoesNotThrow(() -> {
            wf.verifier(NOUVELLE, QUALIFIEE, Role.AGENT);
            wf.verifier(QUALIFIEE, AFFECTEE, Role.AGENT);
            wf.verifier(AFFECTEE, EN_COURS, Role.AGENT);
            wf.verifier(EN_COURS, RESOLUE, Role.AGENT);
            wf.verifier(RESOLUE, CLOTUREE, Role.AGENT);
        });
    }

    @Test
    void transitionInterdite() {
        assertThrows(TransitionInvalideException.class, () -> wf.verifier(NOUVELLE, CLOTUREE, Role.ADMIN));
        assertThrows(TransitionInvalideException.class, () -> wf.verifier(CLOTUREE, EN_COURS, Role.ADMIN));
    }

    @Test
    void rejetReserveAuSuperviseur() {
        assertThrows(TransitionInvalideException.class, () -> wf.verifier(NOUVELLE, REJETEE, Role.AGENT));
        assertDoesNotThrow(() -> wf.verifier(NOUVELLE, REJETEE, Role.SUPERVISEUR));
    }

    @Test
    void repriseEscaladeReserveeAuSuperviseur() {
        assertThrows(TransitionInvalideException.class, () -> wf.verifier(ESCALADEE, EN_COURS, Role.AGENT));
        assertDoesNotThrow(() -> wf.verifier(ESCALADEE, EN_COURS, Role.SUPERVISEUR));
    }
}

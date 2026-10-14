package tn.stb.reclamations.model;

/** États du cycle de vie d'une réclamation. */
public enum Statut {
    NOUVELLE, QUALIFIEE, AFFECTEE, EN_COURS, EN_ATTENTE_CLIENT, ESCALADEE, RESOLUE, CLOTUREE, REJETEE;

    public boolean estTerminal() {
        return this == CLOTUREE || this == REJETEE;
    }
}

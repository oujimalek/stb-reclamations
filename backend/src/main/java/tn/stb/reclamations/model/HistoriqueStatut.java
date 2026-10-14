package tn.stb.reclamations.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Trace d'audit : chaque changement de statut est historisé. */
@Entity
@Table(name = "historique_statuts")
public class HistoriqueStatut {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Reclamation reclamation;
    @Enumerated(EnumType.STRING)
    private Statut ancienStatut;
    @Enumerated(EnumType.STRING)
    private Statut nouveauStatut;
    private String auteur;
    private String motif;
    private LocalDateTime date = LocalDateTime.now();

    public HistoriqueStatut() {}

    public HistoriqueStatut(Reclamation r, Statut ancien, Statut nouveau, String auteur, String motif) {
        this.reclamation = r; this.ancienStatut = ancien; this.nouveauStatut = nouveau; this.auteur = auteur; this.motif = motif;
    }

    public Long getId() { return id; }
    public Reclamation getReclamation() { return reclamation; }
    public Statut getAncienStatut() { return ancienStatut; }
    public Statut getNouveauStatut() { return nouveauStatut; }
    public String getAuteur() { return auteur; }
    public String getMotif() { return motif; }
    public LocalDateTime getDate() { return date; }
}

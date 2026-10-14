package tn.stb.reclamations.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commentaires")
public class Commentaire {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Reclamation reclamation;
    private String auteur;
    @Column(length = 2000)
    private String contenu;
    /** true : visible uniquement par les agents ; false : réponse envoyée au client. */
    private boolean interne;
    private LocalDateTime date = LocalDateTime.now();

    public Commentaire() {}

    public Commentaire(Reclamation r, String auteur, String contenu, boolean interne) {
        this.reclamation = r; this.auteur = auteur; this.contenu = contenu; this.interne = interne;
    }

    public Long getId() { return id; }
    public Reclamation getReclamation() { return reclamation; }
    public String getAuteur() { return auteur; }
    public String getContenu() { return contenu; }
    public boolean isInterne() { return interne; }
    public LocalDateTime getDate() { return date; }
}

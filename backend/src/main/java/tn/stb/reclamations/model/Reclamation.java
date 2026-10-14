package tn.stb.reclamations.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reclamations")
public class Reclamation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String reference;
    @Column(nullable = false)
    private String objet;
    @Column(nullable = false, length = 5000)
    private String description;
    @Enumerated(EnumType.STRING)
    private Canal canal;
    @Enumerated(EnumType.STRING)
    private Categorie categorie;
    /** Catégorie proposée par le modèle NLP (conservée pour mesurer la qualité du modèle). */
    @Enumerated(EnumType.STRING)
    private Categorie categorieSuggeree;
    private Double confianceNlp;
    @Enumerated(EnumType.STRING)
    private Priorite priorite;
    @Enumerated(EnumType.STRING)
    private Statut statut = Statut.NOUVELLE;
    @ManyToOne(optional = false)
    private Client client;
    @ManyToOne
    private Utilisateur agent;
    private LocalDateTime dateCreation = LocalDateTime.now();
    private LocalDateTime dateEcheance;
    private LocalDateTime dateCloture;
    private boolean escaladee;
    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    private List<HistoriqueStatut> historique = new ArrayList<>();
    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC")
    private List<Commentaire> commentaires = new ArrayList<>();

    public boolean estEnRetard() {
        return !statut.estTerminal() && statut != Statut.RESOLUE
                && dateEcheance != null && LocalDateTime.now().isAfter(dateEcheance);
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public Categorie getCategorieSuggeree() { return categorieSuggeree; }
    public void setCategorieSuggeree(Categorie categorieSuggeree) { this.categorieSuggeree = categorieSuggeree; }
    public Double getConfianceNlp() { return confianceNlp; }
    public void setConfianceNlp(Double confianceNlp) { this.confianceNlp = confianceNlp; }
    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Utilisateur getAgent() { return agent; }
    public void setAgent(Utilisateur agent) { this.agent = agent; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDateTime dateEcheance) { this.dateEcheance = dateEcheance; }
    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    public boolean isEscaladee() { return escaladee; }
    public void setEscaladee(boolean escaladee) { this.escaladee = escaladee; }
    public List<HistoriqueStatut> getHistorique() { return historique; }
    public List<Commentaire> getCommentaires() { return commentaires; }
}

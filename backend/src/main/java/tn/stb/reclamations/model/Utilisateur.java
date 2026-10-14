package tn.stb.reclamations.model;

import jakarta.persistence.*;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String motDePasse;
    private String nomComplet;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean actif = true;
    /** Catégories de réclamations que l'agent est habilité à traiter. */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Categorie> specialites = new HashSet<>();

    public Utilisateur() {}

    public Utilisateur(String email, String motDePasse, String nomComplet, Role role, Set<Categorie> specialites) {
        this.email = email; this.motDePasse = motDePasse; this.nomComplet = nomComplet; this.role = role;
        this.specialites = specialites == null ? EnumSet.noneOf(Categorie.class) : new HashSet<>(specialites);
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Set<Categorie> getSpecialites() { return specialites; }
    public void setSpecialites(Set<Categorie> specialites) { this.specialites = specialites; }
}

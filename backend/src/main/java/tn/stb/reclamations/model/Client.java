package tn.stb.reclamations.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomComplet;
    @Column(unique = true)
    private String email;
    private String telephone;
    private String numeroCompte;

    public Client() {}

    public Client(String nomComplet, String email, String telephone, String numeroCompte) {
        this.nomComplet = nomComplet; this.email = email; this.telephone = telephone; this.numeroCompte = numeroCompte;
    }

    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }
}
